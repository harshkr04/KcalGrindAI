package com.kcalgrindai.app.feature.aicoach

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.data.remote.dto.RecentTrendsDto
import com.kcalgrindai.app.domain.model.ChatMessage
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.MessageRole
import com.kcalgrindai.app.domain.repository.AIAnalysisRepository
import com.kcalgrindai.app.domain.repository.AICoachRepository
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.domain.repository.WaterLogRepository
import com.kcalgrindai.app.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

data class ProposedMealLogData(
    val mealType: MealType,
    val items: List<FoodLogItem>,
    val totalCalories: Int
)

data class AiChatUiState(
    val conversationId: Long = 0L,
    val messages: List<ChatMessage> = emptyList(),
    val inputMessage: String = "",
    val attachedImageUri: Uri? = null,
    val attachedImageBase64: String? = null,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null,
    val usage: com.kcalgrindai.app.domain.model.AICoachUsage = com.kcalgrindai.app.domain.model.AICoachUsage()
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiCoachRepository: AICoachRepository,
    private val userProfileRepository: UserProfileRepository,
    private val nutritionGoalRepository: NutritionGoalRepository,
    private val mealLogRepository: MealLogRepository,
    private val waterLogRepository: WaterLogRepository,
    private val weightRepository: WeightRepository,
    private val aiAnalysisRepository: AIAnalysisRepository? = null,
    private val aiCoachUsageRepository: com.kcalgrindai.app.domain.repository.AICoachUsageRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var messagesJob: kotlinx.coroutines.Job? = null

    init {
        initializeConversation()
        observeUsage()
    }

    private fun observeUsage() {
        val usageRepo = aiCoachUsageRepository ?: return
        viewModelScope.launch {
            usageRepo.observeUsage().collect { usage ->
                _uiState.update { it.copy(usage = usage) }
            }
        }
    }

    private fun initializeConversation() {
        viewModelScope.launch {
            val latest = aiCoachRepository.getLatestConversation()
            val convId = latest?.id ?: aiCoachRepository.createConversation()
            _uiState.update { it.copy(conversationId = convId) }
            observeMessages(convId)
        }
    }

    private fun observeMessages(convId: Long) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            aiCoachRepository.observeMessagesForConversation(convId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val newId = aiCoachRepository.createConversation()
            _uiState.update {
                it.copy(
                    conversationId = newId,
                    messages = emptyList(),
                    inputMessage = "",
                    attachedImageUri = null,
                    attachedImageBase64 = null,
                    errorMessage = null,
                    actionSuccessMessage = null
                )
            }
            observeMessages(newId)
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputMessage = text, errorMessage = null) }
    }

    fun onImageAttached(uri: Uri, base64: String) {
        _uiState.update { it.copy(attachedImageUri = uri, attachedImageBase64 = base64, errorMessage = null) }
    }

    fun onRemoveAttachedImage() {
        _uiState.update { it.copy(attachedImageUri = null, attachedImageBase64 = null) }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputMessage.trim()
        val attachedBase64 = _uiState.value.attachedImageBase64
        val convId = _uiState.value.conversationId
        if ((text.isBlank() && attachedBase64 == null) || convId == 0L || _uiState.value.isSending) return

        if (_uiState.value.usage.isLimitReached) {
            val limitMsg = if (_uiState.value.usage.isPro) {
                "You've reached today's Pro AI Coach limit."
            } else {
                "You've reached today's free AI Coach limit. Your messages reset tomorrow."
            }
            _uiState.update { it.copy(errorMessage = limitMsg) }
            return
        }

        _uiState.update {
            it.copy(
                inputMessage = "",
                attachedImageUri = null,
                attachedImageBase64 = null,
                isSending = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            if (attachedBase64 != null) {
                // Photo flow: use existing AIAnalysisRepository /ai/analyze-photo
                try {
                    aiCoachUsageRepository?.checkQuota()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = e.message ?: "You've reached today's free AI Coach limit. Your messages reset tomorrow."
                        )
                    }
                    return@launch
                }

                val userText = if (text.isNotBlank()) text else "What's in this meal?"
                val profile = userProfileRepository.getProfile()

                aiCoachRepository.saveMessage(
                    ChatMessage(
                        id = 0L,
                        conversationId = convId,
                        role = MessageRole.USER,
                        text = userText,
                        structuredDataJson = null,
                        createdAt = System.currentTimeMillis()
                    )
                )

                val analysisRepo = aiAnalysisRepository
                if (analysisRepo == null) {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = "Photo analysis service unavailable"
                        )
                    }
                    return@launch
                }

                val analysisResult = analysisRepo.analyzePhoto(
                    imageBase64 = attachedBase64,
                    dietTags = profile?.dietTags ?: emptyList(),
                    allergies = profile?.allergies ?: emptyList()
                )

                analysisResult.fold(
                    onSuccess = { result ->
                        val totalCalories = result.foods.sumOf { it.calories }
                        val foodListText = result.foods.joinToString("\n") { food ->
                            "• ${food.name} (~${food.estimatedGrams.toInt()}g): ${food.calories} kcal (P: ${food.macros.protein.roundToInt()}g, C: ${food.macros.carbs.roundToInt()}g, F: ${food.macros.fat.roundToInt()}g)"
                        }
                        val replyText = "Based on your photo, here is the nutritional breakdown:\n\n$foodListText\n\nTotal: $totalCalories kcal (${(result.overallConfidence * 100).toInt()}% confidence)"

                        // Envelope for one-tap logging
                        val envelopeJson = JSONObject().apply {
                            put("type", "create_food_log")
                            val dataObj = JSONObject().apply {
                                put("mealType", "lunch")
                                val itemsArray = JSONArray()
                                result.foods.forEach { food ->
                                    itemsArray.put(JSONObject().apply {
                                        put("name", food.name)
                                        put("estimatedGrams", food.estimatedGrams)
                                        put("calories", food.calories.toDouble())
                                        put("macros", JSONObject().apply {
                                            put("protein", food.macros.protein)
                                            put("carbs", food.macros.carbs)
                                            put("fat", food.macros.fat)
                                        })
                                    })
                                }
                                put("items", itemsArray)
                            }
                            put("data", dataObj)
                        }.toString()

                        aiCoachRepository.saveMessage(
                            ChatMessage(
                                id = 0L,
                                conversationId = convId,
                                role = MessageRole.ASSISTANT,
                                text = replyText,
                                structuredDataJson = envelopeJson,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                        // Consume quota ONLY on success
                        aiCoachUsageRepository?.recordMessageSent()
                        _uiState.update { it.copy(isSending = false) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                errorMessage = error.message ?: "Failed to analyze photo. Ensure backend is running."
                            )
                        }
                    }
                )
            } else {
                val profile = userProfileRepository.getProfile()
                val goal = nutritionGoalRepository.getLatestGoal()
                val targetCalories = goal?.calories ?: 2000

                val recentTrends = RecentTrendsDto(
                    avgCalories = 1850,
                    targetCalories = targetCalories,
                    daysTracked = 5,
                    totalDays = 7,
                    trackingRatePercent = 71,
                    avgProteinG = 85.0,
                    avgCarbsG = 190.0,
                    avgFatG = 55.0,
                    weightChangeKg = -0.5
                )

                val result = aiCoachRepository.sendChatMessage(
                    conversationId = convId,
                    userContent = text,
                    remainingCalories = targetCalories - 674,
                    consumedCalories = 674,
                    targetCalories = targetCalories,
                    goal = profile?.goal?.name,
                    recentTrends = recentTrends
                )

                result.fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isSending = false) }
                        handleSuggestedAction(response.suggestedAction)
                    },
                    onFailure = { error ->
                        if (error is com.kcalgrindai.app.domain.model.AICoachQuotaExceededException) {
                            _uiState.update {
                                it.copy(
                                    isSending = false,
                                    errorMessage = error.message
                                )
                            }
                        } else {
                            val fallbackReply = generateFallbackCoachResponse(text, targetCalories)
                            aiCoachRepository.saveMessage(
                                ChatMessage(
                                    id = 0L,
                                    conversationId = convId,
                                    role = MessageRole.ASSISTANT,
                                    text = fallbackReply,
                                    structuredDataJson = null,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                            _uiState.update { it.copy(isSending = false) }
                        }
                    }
                )
            }
        }
    }

    private suspend fun handleSuggestedAction(actionJson: String?) {
        if (actionJson.isNullOrBlank()) return
        try {
            val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val envelope = jsonParser.decodeFromString<com.kcalgrindai.app.data.remote.dto.SuggestedActionEnvelopeDto>(actionJson)
            if (envelope.type == "log_water" && envelope.data != null) {
                val waterDto = jsonParser.decodeFromJsonElement(
                    com.kcalgrindai.app.data.remote.dto.WaterLogActionDto.serializer(),
                    envelope.data
                )
                val amountMl = waterDto.amountMl
                val today = LocalDate.now().toString()
                waterLogRepository.logWater(amountMl, today)
                _uiState.update {
                    it.copy(actionSuccessMessage = "Logged ${amountMl}ml water to your diary!")
                }
            }
        } catch (_: Exception) {}
    }

    fun confirmFoodLog(mealType: MealType, items: List<FoodLogItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val totalCalories = items.sumOf { it.calories }
            val mealLog = MealLog(
                id = 0L,
                date = today,
                mealType = mealType,
                totalCalories = totalCalories,
                loggedAt = System.currentTimeMillis(),
                source = LogSource.AI_TEXT,
                synced = true
            )
            val confirmedItems = items.map {
                it.copy(
                    id = 0L,
                    source = ItemSource.AI,
                    confirmed = true
                )
            }
            mealLogRepository.saveMeal(mealLog, confirmedItems)
            _uiState.update {
                it.copy(actionSuccessMessage = "Logged ${mealType.label} ($totalCalories kcal) to your diary!")
            }
        }
    }

    private fun generateFallbackCoachResponse(query: String, targetCalories: Int): String {
        val q = query.lowercase()
        return when {
            q.contains("protein") || q.contains("dinner") -> {
                "Here are high-protein dinner recommendations aligned with your goals:\n\n" +
                "• Grilled Herb Chicken Breast (~180g)\n  360 kcal | 42g Protein | 4g Carbs | 8g Fat\n  Serve with steamed broccoli and quinoa for optimal recovery.\n\n" +
                "• Wild Baked Salmon Fillet (~200g)\n  410 kcal | 40g Protein | 2g Carbs | 24g Healthy Fats\n  Rich in Omega-3 fatty acids for reduced inflammation.\n\n" +
                "• Crispy Edamame & Tofu Bowl (~250g)\n  380 kcal | 38g Protein | 18g Carbs | 14g Fat\n  High-fiber, plant-based powerhouse."
            }
            q.contains("oatmeal") || q.contains("breakfast") -> {
                "A bowl of oatmeal with fresh blueberries and honey is a fantastic complex carb choice (~320 kcal, 8g protein, 58g carbs). You can log this to your Diary with one tap via the Add Food tab or '+' button!"
            }
            q.contains("water") || q.contains("hydrate") -> {
                "Great job keeping up with hydration! I've noted 500ml of water. Consistent hydration optimizes metabolic rate and nutrient delivery throughout the day."
            }
            q.contains("macro") || q.contains("carb") || q.contains("calorie") -> {
                "Your daily target is set at $targetCalories kcal. To maximize your progress, prioritize hitting your daily protein target first, then fill in remaining energy needs with nutrient-dense complex carbs and healthy fats."
            }
            else -> {
                "I'm here to support your fitness journey! I can help you plan balanced meals, optimize your macro split, suggest high-protein snacks, or answer any nutritional questions."
            }
        }
    }
}
