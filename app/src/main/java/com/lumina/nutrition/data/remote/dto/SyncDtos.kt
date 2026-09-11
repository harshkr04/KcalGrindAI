package com.lumina.nutrition.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// =============================================================
// Push Request — mirrors the JSON body the backend expects
// =============================================================

@Serializable
data class SyncPushRequest(
    @SerialName("user_profile")
    val userProfile: SyncUserProfileDto? = null,
    @SerialName("nutrition_goal")
    val nutritionGoal: SyncNutritionGoalDto? = null,
    @SerialName("meal_logs")
    val mealLogs: List<SyncMealLogDto> = emptyList(),
    @SerialName("food_log_items")
    val foodLogItems: List<SyncFoodLogItemDto> = emptyList(),
    @SerialName("weight_entries")
    val weightEntries: List<SyncWeightEntryDto> = emptyList(),
    @SerialName("water_logs")
    val waterLogs: List<SyncWaterLogDto> = emptyList(),
    @SerialName("ai_analyses")
    val aiAnalyses: List<SyncAIAnalysisDto> = emptyList(),
    @SerialName("conversations")
    val conversations: List<SyncConversationDto> = emptyList(),
    @SerialName("messages")
    val messages: List<SyncMessageDto> = emptyList()
)

// =============================================================
// Push Response
// =============================================================

@Serializable
data class SyncPushResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("summary")
    val summary: SyncPushSummary? = null,
    @SerialName("error")
    val error: String? = null
)

@Serializable
data class SyncPushSummary(
    @SerialName("pushed")
    val pushed: Map<String, Int> = emptyMap(),
    @SerialName("skipped")
    val skipped: Map<String, Int> = emptyMap(),
    @SerialName("errors")
    val errors: Map<String, String> = emptyMap()
)

// =============================================================
// Pull Response
// =============================================================

@Serializable
data class SyncPullResponse(
    @SerialName("user_profile")
    val userProfile: SyncPullUserProfileDto? = null,
    @SerialName("nutrition_goal")
    val nutritionGoal: SyncPullNutritionGoalDto? = null,
    @SerialName("meal_logs")
    val mealLogs: List<SyncPullMealLogDto> = emptyList(),
    @SerialName("food_log_items")
    val foodLogItems: List<SyncPullFoodLogItemDto> = emptyList(),
    @SerialName("weight_entries")
    val weightEntries: List<SyncPullWeightEntryDto> = emptyList(),
    @SerialName("water_logs")
    val waterLogs: List<SyncPullWaterLogDto> = emptyList(),
    @SerialName("conversations")
    val conversations: List<SyncPullConversationDto> = emptyList(),
    @SerialName("messages")
    val messages: List<SyncPullMessageDto> = emptyList(),
    @SerialName("synced_at")
    val syncedAt: String = ""
)

// =============================================================
// Entity DTOs for Push (Room → Backend)
// Uses camelCase property names matching what server.js expects
// =============================================================

@Serializable
data class SyncUserProfileDto(
    @SerialName("email")
    val email: String? = null,
    @SerialName("goal")
    val goal: String,
    @SerialName("units")
    val units: String,
    @SerialName("age")
    val age: Int,
    @SerialName("heightCm")
    val heightCm: Double,
    @SerialName("weightKg")
    val weightKg: Double,
    @SerialName("goalWeightKg")
    val goalWeightKg: Double,
    @SerialName("activityLevel")
    val activityLevel: String,
    @SerialName("dietTags")
    val dietTags: List<String> = emptyList(),
    @SerialName("allergies")
    val allergies: List<String> = emptyList(),
    @SerialName("targetsSource")
    val targetsSource: String,
    @SerialName("createdAt")
    val createdAt: Long,
    @SerialName("updatedAt")
    val updatedAt: Long
)

@Serializable
data class SyncNutritionGoalDto(
    @SerialName("calories")
    val calories: Int,
    @SerialName("proteinG")
    val proteinG: Double,
    @SerialName("carbsG")
    val carbsG: Double,
    @SerialName("fatG")
    val fatG: Double,
    @SerialName("waterLiters")
    val waterLiters: Double,
    @SerialName("waterGlasses")
    val waterGlasses: Int,
    @SerialName("bmr")
    val bmr: Double? = null,
    @SerialName("tdee")
    val tdee: Double? = null,
    @SerialName("isCustom")
    val isCustom: Boolean = false,
    @SerialName("updatedAt")
    val updatedAt: Long
)

@Serializable
data class SyncMealLogDto(
    @SerialName("id")
    val id: Long,
    @SerialName("date")
    val date: String,
    @SerialName("mealType")
    val mealType: String,
    @SerialName("totalCalories")
    val totalCalories: Double,
    @SerialName("loggedAt")
    val loggedAt: Long,
    @SerialName("source")
    val source: String,
    @SerialName("synced")
    val synced: Boolean
)

@Serializable
data class SyncFoodLogItemDto(
    @SerialName("id")
    val id: Long,
    @SerialName("mealLogId")
    val mealLogId: Long,
    @SerialName("foodId")
    val foodId: Long? = null,
    @SerialName("name")
    val name: String,
    @SerialName("brand")
    val brand: String? = null,
    @SerialName("servingDescription")
    val servingDescription: String,
    @SerialName("servingGrams")
    val servingGrams: Double,
    @SerialName("calories")
    val calories: Double,
    @SerialName("proteinG")
    val proteinG: Double,
    @SerialName("carbsG")
    val carbsG: Double,
    @SerialName("fatG")
    val fatG: Double,
    @SerialName("fiberG")
    val fiberG: Double,
    @SerialName("source")
    val source: String,
    @SerialName("confidence")
    val confidence: Float? = null,
    @SerialName("confirmed")
    val confirmed: Boolean
)

@Serializable
data class SyncWeightEntryDto(
    @SerialName("id")
    val id: Long,
    @SerialName("weightKg")
    val weightKg: Double,
    @SerialName("date")
    val date: String,
    @SerialName("note")
    val note: String? = null,
    @SerialName("loggedAt")
    val loggedAt: Long
)

@Serializable
data class SyncWaterLogDto(
    @SerialName("id")
    val id: Long,
    @SerialName("date")
    val date: String,
    @SerialName("amountMl")
    val amountMl: Int,
    @SerialName("loggedAt")
    val loggedAt: Long
)

@Serializable
data class SyncAIAnalysisDto(
    @SerialName("id")
    val id: Long,
    @SerialName("inputType")
    val inputType: String,
    @SerialName("rawInputRef")
    val rawInputRef: String? = null,
    @SerialName("resultJson")
    val resultJson: String,
    @SerialName("overallConfidence")
    val overallConfidence: Float? = null,
    @SerialName("createdAt")
    val createdAt: Long
)

@Serializable
data class SyncConversationDto(
    @SerialName("id")
    val id: Long,
    @SerialName("startedAt")
    val startedAt: Long,
    @SerialName("lastMessageAt")
    val lastMessageAt: Long
)

@Serializable
data class SyncMessageDto(
    @SerialName("id")
    val id: Long,
    @SerialName("conversationId")
    val conversationId: Long,
    @SerialName("role")
    val role: String,
    @SerialName("text")
    val text: String,
    @SerialName("structuredDataJson")
    val structuredDataJson: String? = null,
    @SerialName("createdAt")
    val createdAt: Long
)

// =============================================================
// Pull DTOs (Backend → Room) — use snake_case matching Supabase
// =============================================================

@Serializable
data class SyncPullUserProfileDto(
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("goal")
    val goal: String,
    @SerialName("units")
    val units: String,
    @SerialName("age")
    val age: Int,
    @SerialName("height_cm")
    val heightCm: Double,
    @SerialName("weight_kg")
    val weightKg: Double,
    @SerialName("goal_weight_kg")
    val goalWeightKg: Double,
    @SerialName("activity_level")
    val activityLevel: String,
    @SerialName("diet_tags")
    val dietTags: List<String> = emptyList(),
    @SerialName("allergies")
    val allergies: List<String> = emptyList(),
    @SerialName("targets_source")
    val targetsSource: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class SyncPullNutritionGoalDto(
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("calories")
    val calories: Int,
    @SerialName("protein_g")
    val proteinG: Double,
    @SerialName("carbs_g")
    val carbsG: Double,
    @SerialName("fat_g")
    val fatG: Double,
    @SerialName("water_liters")
    val waterLiters: Double,
    @SerialName("water_glasses")
    val waterGlasses: Int,
    @SerialName("bmr")
    val bmr: Double? = null,
    @SerialName("tdee")
    val tdee: Double? = null,
    @SerialName("is_custom")
    val isCustom: Boolean = false,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class SyncPullMealLogDto(
    @SerialName("id")
    val id: Long,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("local_id")
    val localId: Long? = null,
    @SerialName("log_date")
    val logDate: String,
    @SerialName("meal_type")
    val mealType: String,
    @SerialName("total_calories")
    val totalCalories: Double,
    @SerialName("logged_at")
    val loggedAt: String,
    @SerialName("source")
    val source: String,
    @SerialName("synced")
    val synced: Boolean = true
)

@Serializable
data class SyncPullFoodLogItemDto(
    @SerialName("id")
    val id: Long,
    @SerialName("meal_log_id")
    val mealLogId: Long,
    @SerialName("local_id")
    val localId: Long? = null,
    @SerialName("local_meal_log_id")
    val localMealLogId: Long? = null,
    @SerialName("food_id")
    val foodId: Long? = null,
    @SerialName("name")
    val name: String,
    @SerialName("brand")
    val brand: String? = null,
    @SerialName("serving_description")
    val servingDescription: String,
    @SerialName("serving_grams")
    val servingGrams: Double,
    @SerialName("calories")
    val calories: Double,
    @SerialName("protein_g")
    val proteinG: Double,
    @SerialName("carbs_g")
    val carbsG: Double,
    @SerialName("fat_g")
    val fatG: Double,
    @SerialName("fiber_g")
    val fiberG: Double,
    @SerialName("source")
    val source: String,
    @SerialName("confidence")
    val confidence: Float? = null,
    @SerialName("confirmed")
    val confirmed: Boolean = false
)

@Serializable
data class SyncPullWeightEntryDto(
    @SerialName("id")
    val id: Long,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("local_id")
    val localId: Long? = null,
    @SerialName("weight_kg")
    val weightKg: Double,
    @SerialName("log_date")
    val logDate: String,
    @SerialName("note")
    val note: String? = null,
    @SerialName("logged_at")
    val loggedAt: String
)

@Serializable
data class SyncPullWaterLogDto(
    @SerialName("id")
    val id: Long,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("local_id")
    val localId: Long? = null,
    @SerialName("log_date")
    val logDate: String,
    @SerialName("amount_ml")
    val amountMl: Int,
    @SerialName("logged_at")
    val loggedAt: String
)

@Serializable
data class SyncPullConversationDto(
    @SerialName("id")
    val id: Long,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("local_id")
    val localId: Long? = null,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("last_message_at")
    val lastMessageAt: String
)

@Serializable
data class SyncPullMessageDto(
    @SerialName("id")
    val id: Long,
    @SerialName("conversation_id")
    val conversationId: Long,
    @SerialName("local_id")
    val localId: Long? = null,
    @SerialName("local_conversation_id")
    val localConversationId: Long? = null,
    @SerialName("role")
    val role: String,
    @SerialName("text_content")
    val textContent: String,
    @SerialName("structured_data")
    val structuredData: JsonElement? = null,
    @SerialName("created_at")
    val createdAt: String
)
