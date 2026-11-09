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
import kotlinx.coroutines.flow.first
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
                        val friendlyMessage = when {
                            error.message?.contains("429") == true || error is com.kcalgrindai.app.domain.model.AICoachQuotaExceededException ->
                                "You've reached today's free photo analysis limit. Your quota resets tomorrow."
                            error.message?.contains("503") == true ->
                                "The AI service is temporarily experiencing high traffic. Please try again in a few moments."
                            error.message?.contains("timeout", ignoreCase = true) == true ->
                                "The request timed out. Please check your internet connection and try again."
                            error.message?.contains("offline", ignoreCase = true) == true || error.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                                "You appear to be offline. Please reconnect to the internet to analyze photos."
                            else ->
                                error.message ?: "Failed to analyze photo. Ensure backend is running."
                        }
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                errorMessage = friendlyMessage
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

                val todayIso = LocalDate.now().toString()
                val consumedCalories = getDailyConsumedCalories(todayIso)
                val remainingCalories = targetCalories - consumedCalories

                // Pre-flight client safety check: intercept crisis/eating-disorder cues before server call.
                // Guarantees zero quota consumption and instantaneous local support.
                if (checkClientCrisisKeywords(text)) {
                    val crisisReply = getRegionAwareCrisisResponse()
                    aiCoachRepository.saveMessage(
                        ChatMessage(
                            id = 0L,
                            conversationId = convId,
                            role = MessageRole.ASSISTANT,
                            text = crisisReply,
                            structuredDataJson = "{\"crisis_intercepted\": true, \"source\": \"client_safety_guardrail\"}",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = null
                        )
                    }
                    return@launch
                }

                val result = aiCoachRepository.sendChatMessage(
                    conversationId = convId,
                    userContent = text,
                    remainingCalories = remainingCalories,
                    consumedCalories = consumedCalories,
                    targetCalories = targetCalories,
                    goal = profile?.goal?.toHumanLabel() ?: "maintain weight",
                    recentTrends = recentTrends
                )

                result.fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isSending = false) }
                        handleSuggestedAction(response.suggestedAction)
                    },
                    onFailure = { error ->
                        if (error is com.kcalgrindai.app.domain.model.AICoachQuotaExceededException || error.message?.contains("429") == true) {
                            val limitMsg = error.message ?: "You've reached today's free AI Coach limit. Your messages reset tomorrow."
                            _uiState.update {
                                it.copy(
                                    isSending = false,
                                    errorMessage = limitMsg
                                )
                            }
                        } else {
                            if (checkClientCrisisKeywords(text)) {
                                aiCoachRepository.saveMessage(
                                    ChatMessage(
                                        id = 0L,
                                        conversationId = convId,
                                        role = MessageRole.ASSISTANT,
                                        text = getRegionAwareCrisisResponse(),
                                        structuredDataJson = "{\"crisis_intercepted\": true, \"source\": \"client_safety_guardrail\"}",
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                _uiState.update {
                                    it.copy(
                                        isSending = false,
                                        errorMessage = null
                                    )
                                }
                            } else {
                                val summaryReply = buildDeterministicOfflineSummary(targetCalories, goal, todayIso)
                                aiCoachRepository.saveMessage(
                                    ChatMessage(
                                        id = 0L,
                                        conversationId = convId,
                                        role = MessageRole.ASSISTANT,
                                        text = summaryReply,
                                        structuredDataJson = "{\"is_fallback\": true, \"source\": \"offline_summary\"}",
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                _uiState.update {
                                    it.copy(
                                        isSending = false,
                                        errorMessage = "Sproutling needs an internet connection"
                                    )
                                }
                            }
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

    internal suspend fun getDailyConsumedCalories(dateIso: String): Int {
        return getDailyConsumedNutrition(dateIso).calories
    }

    internal suspend fun getDailyConsumedNutrition(dateIso: String): ConsumedDailyNutrition {
        return try {
            val meals = mealLogRepository.observeMealsWithItemsByDate(dateIso).first()
            var cal = 0.0
            var p = 0.0
            var c = 0.0
            var f = 0.0
            for (mealWithItems in meals) {
                if (mealWithItems.items.isNotEmpty()) {
                    for (item in mealWithItems.items) {
                        cal += item.calories
                        p += item.proteinG
                        c += item.carbsG
                        f += item.fatG
                    }
                } else {
                    cal += mealWithItems.meal.totalCalories
                }
            }
            ConsumedDailyNutrition(
                calories = cal.roundToInt(),
                proteinG = p,
                carbsG = c,
                fatG = f
            )
        } catch (_: Exception) {
            ConsumedDailyNutrition()
        }
    }

    internal suspend fun buildDeterministicOfflineSummary(
        targetCalories: Int,
        goal: com.kcalgrindai.app.domain.model.NutritionGoal?,
        dateIso: String
    ): String {
        val consumed = getDailyConsumedNutrition(dateIso)
        val remainingCalories = (targetCalories - consumed.calories).coerceAtLeast(0)

        val targetProtein = (goal?.proteinG ?: (targetCalories * 0.25 / 4.0)).roundToInt()
        val consumedProtein = consumed.proteinG.roundToInt()
        val remainingProtein = (targetProtein - consumedProtein).coerceAtLeast(0)

        val targetCarbs = (goal?.carbsG ?: (targetCalories * 0.50 / 4.0)).roundToInt()
        val consumedCarbs = consumed.carbsG.roundToInt()
        val remainingCarbs = (targetCarbs - consumedCarbs).coerceAtLeast(0)

        val targetFat = (goal?.fatG ?: (targetCalories * 0.25 / 9.0)).roundToInt()
        val consumedFat = consumed.fatG.roundToInt()
        val remainingFat = (targetFat - consumedFat).coerceAtLeast(0)

        return buildString {
            appendLine("Sproutling needs an internet connection.")
            appendLine()
            appendLine("Today's remaining budget:")
            appendLine("• Calories: $remainingCalories kcal left (Target: $targetCalories kcal, Consumed: ${consumed.calories} kcal)")
            appendLine("• Protein: ${remainingProtein}g left (Target: ${targetProtein}g)")
            appendLine("• Carbs: ${remainingCarbs}g left (Target: ${targetCarbs}g)")
            append("• Fat: ${remainingFat}g left (Target: ${targetFat}g)")
        }
    }

    companion object {
        data class ConsumedDailyNutrition(
            val calories: Int = 0,
            val proteinG: Double = 0.0,
            val carbsG: Double = 0.0,
            val fatG: Double = 0.0
        )

        fun getRegionAwareCrisisResponse(countryCode: String = java.util.Locale.getDefault().country): String {
            // Default to India + 112 primary regardless of device locale; list other regions below
            return """
                I care deeply about your health and safety, and I cannot provide guidance on extreme restriction, fasting, purging, or self-harm.

                If you are experiencing distress, struggling with eating habits, or feeling overwhelmed, compassionate support is available right now:

                Emergency: 112 (National Emergency Number, India)
                India Helplines:
                • Tele-MANAS (24/7 National Mental Health): 14416 or 1800-891-4416 (telemanas.mohfw.gov.in)
                • KIRAN (24/7 Rehabilitation & Mental Health): 1800-599-0019 (disabilityaffairs.gov.in)
                • AASRA (24/7 Suicide Prevention): +91 98204 66726 (aasra.info)
                • Vandrevala Foundation (24/7 Free Counseling): +91 9999 666 555 (vandrevalafoundation.com)

                United States & Canada:
                • Emergency: 911
                • 988 Suicide & Crisis Lifeline: Call or text 988 (988lifeline.org)
                • National Association of Anorexia Nervosa and Associated Disorders (ANAD): 1-888-375-7767 (anad.org)
                • Crisis Text Line: Text HOME to 741741 (crisistextline.org)

                United Kingdom:
                • Emergency: 999 or 112
                • Samaritans: 116 123 (samaritans.org)
                • Beat Eating Disorders Helpline: 0808 801 0677 (beateatingdisorders.org.uk)
                • Shout Crisis Text Line: Text SHOUT to 85258 (giveusashout.org)

                International:
                • Befrienders Worldwide: befrienders.org
                • Find A Helpline (worldwide directory): findahelpline.com

                Please reach out to one of these services or speak with a licensed healthcare provider who can support you safely.
            """.trimIndent()
        }

        val CRISIS_SUPPORT_RESPONSE_TEXT: String
            get() = getRegionAwareCrisisResponse()

        private val CLIENT_CRISIS_PATTERNS = listOf(
            Regex("""\b(kill\s+myself|kil{1,2}\s+myself|commit\s+suicide|suicid[ea]?|suicde|sucide|end\s+my\s+life|want\s+to\s+die|suicidal)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(starve\s+myself|starr?v(e|ing)\s+(myself|to\s+death)|how\s+to\s+starr?ve|starving\s+myself|starve\s+to\s+death|starve\b)""", RegexOption.IGNORE_CASE),
            Regex("""\b(purge|purging|purgg?ing|purg(e|ed))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(make\s+myself\s+throw\s*up|throw\s*up\s+after\s+(eating|dinner|food|meals?))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(vom+it(ing)?\s+after\s+(eating|meals?|food))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(laxatives?\s+(to\s+empty|after|for\s+weight)|empty\s+my\s+stomach\s+(with\s+laxatives?|after))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(chew\s+and\s+spit)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(hide\s+(my\s+)?(anorexia|bulimia|eating\s+disorder)|anorexia|bulimia|anorexic|bulimic)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(punish\s+myself\s+by\s+(not\s+eating|starving))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(\d+[- ]day\s+(water\s+)?fast|water\s+fast(ing)?|dry\s+fast(ing)?|fast\s+(for\s+)?(7|10|14|21|30)\s+days)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(eat(ing)?\s+(only\s+)?(0|50|100|200|300|500)\s+calories(\s+a\s+day)?)\b""", RegexOption.IGNORE_CASE),
            // Hinglish crisis patterns
            Regex("""\b(mar\s+ja(na|un|unga|ungi|na\s+chahta|na\s+chahti)|jaan\s+de\s+(dunga|dungi|dena)|zindagi\s+khatam|suicide\s+karn(a|i|e))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(khana\s+(chhod\s+(dunga|dungi|dena)|chhodna|chhod\s+diya)|bhukh(a|i)\s+rah(na|unga|ungi))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(khana\s+khane\s+ke\s+baad\s+(ulti|vomit)|ulti\s+karn(a|i)\s+khana\s+ke\s+baad)\b""", RegexOption.IGNORE_CASE)
        )

        internal fun checkClientCrisisKeywords(text: String): Boolean {
            return CLIENT_CRISIS_PATTERNS.any { it.containsMatchIn(text) }
        }
    }
}
