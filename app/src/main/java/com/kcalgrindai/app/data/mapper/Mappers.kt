package com.kcalgrindai.app.data.mapper

import com.kcalgrindai.app.data.local.entity.AIAnalysisEntity
import com.kcalgrindai.app.data.local.entity.ConversationEntity
import com.kcalgrindai.app.data.local.entity.FoodEntity
import com.kcalgrindai.app.data.local.entity.FoodLogItemEntity
import com.kcalgrindai.app.data.local.entity.MealLogEntity
import com.kcalgrindai.app.data.local.entity.MessageEntity
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import com.kcalgrindai.app.data.local.entity.UserProfileEntity
import com.kcalgrindai.app.data.local.entity.WeightEntryEntity
import com.kcalgrindai.app.data.local.model.ConversationWithMessages
import com.kcalgrindai.app.data.local.model.MealWithItems
import com.kcalgrindai.app.domain.model.AIAnalysisRecord
import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.ChatMessage
import com.kcalgrindai.app.domain.model.Conversation
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.FoodSource
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.MealWithFoodItems
import com.kcalgrindai.app.domain.model.MessageRole
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.TargetBasis
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.model.WeightEntry

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        firebaseUid = firebaseUid,
        email = email,
        firstName = firstName,
        goal = GoalType.fromId(goal),
        units = UnitSystem.fromId(units),
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        goalWeightKg = goalWeightKg,
        activityLevel = ActivityLevel.fromId(activityLevel),
        dietTags = dietTags,
        allergies = allergies,
        targetsSource = TargetBasis.fromId(targetsSource),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        firebaseUid = firebaseUid,
        email = email,
        firstName = firstName,
        goal = goal.id,
        units = units.id,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        goalWeightKg = goalWeightKg,
        activityLevel = activityLevel.id,
        dietTags = dietTags,
        allergies = allergies,
        targetsSource = targetsSource.id,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NutritionGoalEntity.toDomain(): NutritionGoal {
    return NutritionGoal(
        id = id,
        userId = userId,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        waterLiters = waterLiters,
        waterGlasses = waterGlasses,
        bmr = bmr,
        tdee = tdee,
        isCustom = isCustom,
        updatedAt = updatedAt
    )
}

fun NutritionGoal.toEntity(): NutritionGoalEntity {
    return NutritionGoalEntity(
        id = id,
        userId = userId,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        waterLiters = waterLiters,
        waterGlasses = waterGlasses,
        bmr = bmr,
        tdee = tdee,
        isCustom = isCustom,
        updatedAt = updatedAt
    )
}

fun FoodEntity.toDomain(): FoodItem {
    return FoodItem(
        id = id,
        source = FoodSource.fromId(source),
        externalId = externalId,
        name = name,
        brand = brand,
        servingDescription = servingDescription,
        servingGrams = servingGrams,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        barcodeUpc = barcodeUpc,
        isUserCreated = isUserCreated,
        createdAt = createdAt
    )
}

fun FoodItem.toEntity(): FoodEntity {
    return FoodEntity(
        id = id,
        source = source.id,
        externalId = externalId,
        name = name,
        brand = brand,
        servingDescription = servingDescription,
        servingGrams = servingGrams,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        barcodeUpc = barcodeUpc,
        isUserCreated = isUserCreated,
        createdAt = createdAt
    )
}

fun MealLogEntity.toDomain(): MealLog {
    return MealLog(
        id = id,
        date = date,
        mealType = MealType.fromId(mealType),
        totalCalories = totalCalories,
        loggedAt = loggedAt,
        source = LogSource.fromId(source),
        synced = synced
    )
}

fun MealLog.toEntity(): MealLogEntity {
    return MealLogEntity(
        id = id,
        date = date,
        mealType = mealType.id,
        totalCalories = totalCalories,
        loggedAt = loggedAt,
        source = source.id,
        synced = synced
    )
}

fun FoodLogItemEntity.toDomain(): FoodLogItem {
    return FoodLogItem(
        id = id,
        mealLogId = mealLogId,
        foodId = foodId,
        name = name,
        brand = brand,
        servingDescription = servingDescription,
        servingGrams = servingGrams,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        source = ItemSource.fromId(source),
        confidence = confidence,
        confirmed = confirmed
    )
}

fun FoodLogItem.toEntity(): FoodLogItemEntity {
    return FoodLogItemEntity(
        id = id,
        mealLogId = mealLogId,
        foodId = foodId,
        name = name,
        brand = brand,
        servingDescription = servingDescription,
        servingGrams = servingGrams,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        source = source.id,
        confidence = confidence,
        confirmed = confirmed
    )
}

fun MealWithItems.toDomain(): MealWithFoodItems {
    return MealWithFoodItems(
        meal = meal.toDomain(),
        items = items.map { it.toDomain() }
    )
}

fun WeightEntryEntity.toDomain(): WeightEntry {
    return WeightEntry(
        id = id,
        weightKg = weightKg,
        date = date,
        note = note,
        loggedAt = loggedAt
    )
}

fun WeightEntry.toEntity(): WeightEntryEntity {
    return WeightEntryEntity(
        id = id,
        weightKg = weightKg,
        date = date,
        note = note,
        loggedAt = loggedAt
    )
}

fun com.kcalgrindai.app.data.remote.dto.UsdaFoodItem.toDomain(): FoodItem {
    val nutrients = foodNutrients
    fun findNutrient(vararg ids: Int): Double {
        return nutrients.firstOrNull { it.nutrientId != null && it.nutrientId in ids }?.value ?: 0.0
    }
    fun findNutrientByNumber(vararg numbers: String): Double {
        return nutrients.firstOrNull { it.nutrientNumber in numbers }?.value ?: 0.0
    }

    val cals = findNutrient(1008).takeIf { it > 0 } ?: findNutrientByNumber("208")
    val p = findNutrient(1003).takeIf { it > 0 } ?: findNutrientByNumber("203")
    val fat = findNutrient(1004).takeIf { it > 0 } ?: findNutrientByNumber("204")
    val c = findNutrient(1005).takeIf { it > 0 } ?: findNutrientByNumber("205")
    val fiber = findNutrient(1079).takeIf { it > 0 } ?: findNutrientByNumber("291")

    val sSize = servingSize ?: 100.0
    val sDesc = householdServingFullText ?: servingSizeUnit?.let { "$sSize $it" } ?: "100g"

    return FoodItem(
        id = 0L,
        source = FoodSource.USDA,
        externalId = fdcId.toString(),
        name = description,
        brand = brandName ?: brandOwner,
        servingDescription = sDesc,
        servingGrams = sSize,
        calories = cals,
        proteinG = Math.round(p * 10.0) / 10.0,
        carbsG = Math.round(c * 10.0) / 10.0,
        fatG = Math.round(fat * 10.0) / 10.0,
        fiberG = Math.round(fiber * 10.0) / 10.0,
        barcodeUpc = null,
        isUserCreated = false,
        createdAt = System.currentTimeMillis()
    )
}

fun com.kcalgrindai.app.data.remote.dto.OffProductResponse.toDomain(barcode: String): FoodItem? {
    val prod = product ?: return null
    val nut = prod.nutriments

    val cals = nut?.energyKcal100g
        ?: nut?.energyKcal
        ?: (nut?.energy100g?.let { it / 4.184 })
        ?: 0.0
    val p = nut?.proteins100g ?: 0.0
    val c = nut?.carbohydrates100g ?: 0.0
    val f = nut?.fat100g ?: 0.0
    val fib = nut?.fiber100g ?: 0.0

    val sQty = prod.servingQuantity ?: 100.0
    val sDesc = prod.servingSize ?: "100g"

    return FoodItem(
        id = 0L,
        source = FoodSource.OPEN_FOOD_FACTS,
        externalId = code ?: barcode,
        name = prod.productName ?: prod.genericName ?: "Scanned Product ($barcode)",
        brand = prod.brands,
        servingDescription = sDesc,
        servingGrams = sQty,
        calories = cals,
        proteinG = Math.round(p * 10.0) / 10.0,
        carbsG = Math.round(c * 10.0) / 10.0,
        fatG = Math.round(f * 10.0) / 10.0,
        fiberG = Math.round(fib * 10.0) / 10.0,
        barcodeUpc = barcode,
        isUserCreated = false,
        createdAt = System.currentTimeMillis()
    )
}


fun AIAnalysisEntity.toDomain(): AIAnalysisRecord {
    return AIAnalysisRecord(
        id = id,
        inputType = inputType,
        rawInputRef = rawInputRef,
        resultJson = resultJson,
        overallConfidence = overallConfidence,
        createdAt = createdAt
    )
}

fun AIAnalysisRecord.toEntity(): AIAnalysisEntity {
    return AIAnalysisEntity(
        id = id,
        inputType = inputType,
        rawInputRef = rawInputRef,
        resultJson = resultJson,
        overallConfidence = overallConfidence,
        createdAt = createdAt
    )
}

fun MessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        conversationId = conversationId,
        role = MessageRole.fromId(role),
        text = text,
        structuredDataJson = structuredDataJson,
        createdAt = createdAt
    )
}

fun ChatMessage.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.id,
        text = text,
        structuredDataJson = structuredDataJson,
        createdAt = createdAt
    )
}

fun ConversationEntity.toDomain(messages: List<ChatMessage> = emptyList()): Conversation {
    return Conversation(
        id = id,
        startedAt = startedAt,
        lastMessageAt = lastMessageAt,
        messages = messages
    )
}

fun Conversation.toEntity(): ConversationEntity {
    return ConversationEntity(
        id = id,
        startedAt = startedAt,
        lastMessageAt = lastMessageAt
    )
}

fun ConversationWithMessages.toDomain(): Conversation {
    return Conversation(
        id = conversation.id,
        startedAt = conversation.startedAt,
        lastMessageAt = conversation.lastMessageAt,
        messages = messages.map { it.toDomain() }
    )
}

fun com.kcalgrindai.app.data.local.entity.WaterLogEntity.toDomain(): com.kcalgrindai.app.domain.model.WaterLog {
    return com.kcalgrindai.app.domain.model.WaterLog(
        id = id,
        date = date,
        amountMl = amountMl,
        loggedAt = loggedAt
    )
}

fun com.kcalgrindai.app.domain.model.WaterLog.toEntity(): com.kcalgrindai.app.data.local.entity.WaterLogEntity {
    return com.kcalgrindai.app.data.local.entity.WaterLogEntity(
        id = id,
        date = date,
        amountMl = amountMl,
        loggedAt = loggedAt
    )
}

