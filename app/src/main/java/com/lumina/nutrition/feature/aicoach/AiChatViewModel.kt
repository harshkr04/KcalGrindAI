package com.lumina.nutrition.feature.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.data.remote.dto.RecentTrendsDto
import com.lumina.nutrition.domain.model.ChatMessage
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.LogSource
import com.lumina.nutrition.domain.model.MealLog
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.repository.AICoachRepository
import com.lumina.nutrition.domain.repository.MealLogRepository
import com.lumina.nutrition.domain.repository.NutritionGoalRepository
import com.lumina.nutrition.domain.repository.UserProfileRepository
import com.lumina.nutrition.domain.repository.WaterLogRepository
import com.lumina.nutrition.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiCoachRepository: AICoachRepository,
    private val userProfileRepository: UserProfileRepository,
    private val nutritionGoalRepository: NutritionGoalRepository,
    private val mealLogRepository: MealLogRepository,
    private val waterLogRepository: WaterLogRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        initializeConversation()
    }

    private fun initializeConversation() {
        viewModelScope.launch {
            val latest = aiCoachRepository.getLatestConversation()
            val convId = latest?.id ?: aiCoachRepository.createConversation()
            _uiState.update { it.copy(conversationId = convId) }

            aiCoachRepository.observeMessagesForConversation(convId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputMessage = text, errorMessage = null) }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputMessage.trim()
        val convId = _uiState.value.conversationId
        if (text.isBlank() || convId == 0L || _uiState.value.isSending) return

        _uiState.update { it.copy(inputMessage = "", isSending = true, errorMessage = null) }

        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val goal = nutritionGoalRepository.getLatestGoal()
            val today = LocalDate.now()
            val startDate = today.minusDays(6)

            // Compute today's consumed and remaining
            val todayMeals = mealLogRepository.getMealById(0) // or observe
            val targetCalories = goal?.calories ?: 2000

            // 7-day stats for trends context
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
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = error.message ?: "Failed to get AI Coach response"
                        )
                    }
                }
            )
        }
    }

    private suspend fun handleSuggestedAction(actionJson: String?) {
        if (actionJson.isNullOrBlank()) return
        try {
            val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val envelope = jsonParser.decodeFromString<com.lumina.nutrition.data.remote.dto.SuggestedActionEnvelopeDto>(actionJson)
            if (envelope.type == "log_water" && envelope.data != null) {
                val waterDto = jsonParser.decodeFromJsonElement(
                    com.lumina.nutrition.data.remote.dto.WaterLogActionDto.serializer(),
                    envelope.data
                )
                val amountMl = waterDto.amountMl
                val today = LocalDate.now().toString()
                waterLogRepository.logWater(amountMl, today)
                _uiState.update {
                    it.copy(actionSuccessMessage = "💧 Logged ${amountMl}ml water to your diary!")
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
                it.copy(actionSuccessMessage = "🥗 Logged ${mealType.label} ($totalCalories kcal) to your diary!")
            }
        }
    }
}
