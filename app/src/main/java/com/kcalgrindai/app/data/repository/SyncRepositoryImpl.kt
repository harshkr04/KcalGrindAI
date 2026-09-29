package com.kcalgrindai.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.kcalgrindai.app.data.local.dao.AIAnalysisDao
import com.kcalgrindai.app.data.local.dao.ConversationDao
import com.kcalgrindai.app.data.local.dao.FoodLogItemDao
import com.kcalgrindai.app.data.local.dao.MealLogDao
import com.kcalgrindai.app.data.local.dao.MessageDao
import com.kcalgrindai.app.data.local.dao.NutritionGoalDao
import com.kcalgrindai.app.data.local.dao.UserProfileDao
import com.kcalgrindai.app.data.local.dao.WaterLogDao
import com.kcalgrindai.app.data.local.dao.WeightEntryDao
import com.kcalgrindai.app.data.local.entity.ConversationEntity
import com.kcalgrindai.app.data.local.entity.MealLogEntity
import com.kcalgrindai.app.data.local.entity.MessageEntity
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import com.kcalgrindai.app.data.local.entity.UserProfileEntity
import com.kcalgrindai.app.data.local.entity.WaterLogEntity
import com.kcalgrindai.app.data.local.entity.WeightEntryEntity
import com.kcalgrindai.app.data.remote.api.SyncApiService
import com.kcalgrindai.app.data.remote.dto.SyncAIAnalysisDto
import com.kcalgrindai.app.data.remote.dto.SyncConversationDto
import com.kcalgrindai.app.data.remote.dto.SyncFoodLogItemDto
import com.kcalgrindai.app.data.remote.dto.SyncMealLogDto
import com.kcalgrindai.app.data.remote.dto.SyncMessageDto
import com.kcalgrindai.app.data.remote.dto.SyncNutritionGoalDto
import com.kcalgrindai.app.data.remote.dto.SyncPushRequest
import com.kcalgrindai.app.data.remote.dto.SyncUserProfileDto
import com.kcalgrindai.app.data.remote.dto.SyncWaterLogDto
import com.kcalgrindai.app.data.remote.dto.SyncWeightEntryDto
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.SyncPullResult
import com.kcalgrindai.app.domain.repository.SyncPushResult
import com.kcalgrindai.app.domain.repository.SyncRepository
import com.kcalgrindai.app.domain.repository.SyncResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud sync implementation.
 * Reads all local Room data, pushes to Supabase via backend,
 * then pulls remote changes and merges into Room.
 *
 * Room remains the source of truth — Supabase is a synced copy.
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncApi: SyncApiService,
    private val authRepository: AuthRepository,
    private val userProfileDao: UserProfileDao,
    private val nutritionGoalDao: NutritionGoalDao,
    private val mealLogDao: MealLogDao,
    private val foodLogItemDao: FoodLogItemDao,
    private val weightEntryDao: WeightEntryDao,
    private val waterLogDao: WaterLogDao,
    private val aiAnalysisDao: AIAnalysisDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : SyncRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("kcal_grind_sync", Context.MODE_PRIVATE)
    }

    private val isoFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    // -------------------------------------------------------
    // Push all local data to cloud
    // -------------------------------------------------------
    override suspend fun pushAll(): Result<SyncPushResult> = runCatching {
        // Ensure fresh token before pushing to cloud
        authRepository.getIdToken(forceRefresh = true)

        val profile = userProfileDao.getProfile()
        val goal = nutritionGoalDao.getLatestGoal()
        val mealLogs = mealLogDao.getAllMealLogs()
        val foodLogItems = foodLogItemDao.getAllFoodLogItems()
        val weightEntries = weightEntryDao.getAllWeightEntries()
        val waterLogs = waterLogDao.getAllWaterLogs()
        val aiAnalyses = aiAnalysisDao.getAllAnalyses()
        val conversations = conversationDao.getAllConversations()
        val allMessages = mutableListOf<SyncMessageDto>()

        // Collect messages for each conversation
        for (conv in conversations) {
            val msgs = messageDao.getMessagesForConversation(conv.id)
            allMessages.addAll(msgs.map { it.toSyncDto() })
        }

        val request = SyncPushRequest(
            userProfile = profile?.toSyncDto(),
            nutritionGoal = goal?.toSyncDto(),
            mealLogs = mealLogs.map { it.toSyncDto() },
            foodLogItems = foodLogItems.map { it.toSyncDto() },
            weightEntries = weightEntries.map { it.toSyncDto() },
            waterLogs = waterLogs.map { it.toSyncDto() },
            aiAnalyses = aiAnalyses.map { it.toSyncDto() },
            conversations = conversations.map { it.toSyncDto() },
            messages = allMessages
        )

        val response = syncApi.pushSync(request)
        SyncPushResult(
            pushed = response.summary?.pushed ?: emptyMap(),
            skipped = response.summary?.skipped ?: emptyMap(),
            errors = response.summary?.errors ?: emptyMap()
        )
    }

    // -------------------------------------------------------
    // Pull remote changes since last sync
    // -------------------------------------------------------
    override suspend fun pullSince(sinceEpochMs: Long): Result<SyncPullResult> = runCatching {
        // Ensure fresh token before pulling from cloud
        authRepository.getIdToken(forceRefresh = true)

        val sinceIso = isoFormat.format(Date(sinceEpochMs))
        val response = syncApi.pullSync(sinceIso)
        var result = SyncPullResult()

        // 1. User Profile
        response.userProfile?.let { pulled ->
            val existing = userProfileDao.getProfile()
            val pulledUpdatedAt = parseIsoToEpoch(pulled.updatedAt)

            if (existing == null || pulledUpdatedAt >= existing.updatedAt) {
                val entity = UserProfileEntity(
                    id = existing?.id ?: 0L,
                    firebaseUid = pulled.firebaseUid,
                    email = pulled.email,
                    firstName = existing?.firstName,
                    goal = pulled.goal,
                    units = pulled.units,
                    age = pulled.age,
                    heightCm = pulled.heightCm,
                    weightKg = pulled.weightKg,
                    goalWeightKg = pulled.goalWeightKg,
                    activityLevel = pulled.activityLevel,
                    dietTags = pulled.dietTags,
                    allergies = pulled.allergies,
                    targetsSource = pulled.targetsSource,
                    createdAt = parseIsoToEpoch(pulled.createdAt),
                    updatedAt = pulledUpdatedAt
                )
                userProfileDao.upsertProfile(entity)
                result = result.copy(profileUpdated = true)
            }
        }

        // 2. Nutrition Goal
        response.nutritionGoal?.let { pulled ->
            val profile = userProfileDao.getProfile()
            val userId = profile?.id ?: return@let
            val existing = nutritionGoalDao.getGoalByUserId(userId)
            val pulledUpdatedAt = parseIsoToEpoch(pulled.updatedAt)

            if (existing == null || pulledUpdatedAt >= existing.updatedAt) {
                val entity = NutritionGoalEntity(
                    id = existing?.id ?: 0L,
                    userId = userId,
                    calories = pulled.calories,
                    proteinG = pulled.proteinG,
                    carbsG = pulled.carbsG,
                    fatG = pulled.fatG,
                    waterLiters = pulled.waterLiters,
                    waterGlasses = pulled.waterGlasses,
                    bmr = pulled.bmr,
                    tdee = pulled.tdee,
                    isCustom = pulled.isCustom,
                    updatedAt = pulledUpdatedAt
                )
                nutritionGoalDao.upsertGoal(entity)
                result = result.copy(goalUpdated = true)
            }
        }

        // 3. Meal Logs + Food Log Items
        var mealLogsInserted = 0
        for (pulled in response.mealLogs) {
            val localId = pulled.localId
            val existing = localId?.let { mealLogDao.getMealLogById(it) }
            val pulledLoggedAt = parseIsoToEpoch(pulled.loggedAt)

            if (existing != null && pulledLoggedAt < existing.loggedAt) continue

            val entity = MealLogEntity(
                id = existing?.id ?: 0L,
                date = pulled.logDate,
                mealType = pulled.mealType,
                totalCalories = pulled.totalCalories,
                loggedAt = pulledLoggedAt,
                source = pulled.source,
                synced = true
            )
            val upsertRes = mealLogDao.upsertMealLog(entity)
            val targetMealLogId = if (existing != null && existing.id > 0L) existing.id else upsertRes
            mealLogsInserted++

            // Upsert food log items for this meal (from the pulled response)
            val pulledItems = response.foodLogItems.filter { it.mealLogId == pulled.id }
            for (item in pulledItems) {
                val existingItem = item.localId?.let { foodLogItemDao.getItemById(it) }
                val itemEntity = com.kcalgrindai.app.data.local.entity.FoodLogItemEntity(
                    id = existingItem?.id ?: 0L,
                    mealLogId = targetMealLogId,
                    foodId = item.foodId,
                    name = item.name,
                    brand = item.brand,
                    servingDescription = item.servingDescription,
                    servingGrams = item.servingGrams,
                    calories = item.calories,
                    proteinG = item.proteinG,
                    carbsG = item.carbsG,
                    fatG = item.fatG,
                    fiberG = item.fiberG,
                    source = item.source,
                    confidence = item.confidence,
                    confirmed = item.confirmed
                )
                foodLogItemDao.upsertItem(itemEntity)
            }
        }
        result = result.copy(mealLogsInserted = mealLogsInserted)

        // 4. Weight Entries
        var weightInserted = 0
        for (pulled in response.weightEntries) {
            val existing = pulled.localId?.let { weightEntryDao.getWeightEntryById(it) }
            val pulledLoggedAt = parseIsoToEpoch(pulled.loggedAt)

            if (existing != null && pulledLoggedAt < existing.loggedAt) continue

            val entity = WeightEntryEntity(
                id = existing?.id ?: 0L,
                weightKg = pulled.weightKg,
                date = pulled.logDate,
                note = pulled.note,
                loggedAt = pulledLoggedAt
            )
            weightEntryDao.upsertWeightEntry(entity)
            weightInserted++
        }
        result = result.copy(weightEntriesInserted = weightInserted)

        // 5. Water Logs
        var waterInserted = 0
        for (pulled in response.waterLogs) {
            val existing = pulled.localId?.let { waterLogDao.getWaterLogsByDate(pulled.logDate).find { w -> w.id == pulled.localId } }
            val pulledLoggedAt = parseIsoToEpoch(pulled.loggedAt)

            if (existing != null && pulledLoggedAt < existing.loggedAt) continue

            val entity = WaterLogEntity(
                id = existing?.id ?: 0L,
                date = pulled.logDate,
                amountMl = pulled.amountMl,
                loggedAt = pulledLoggedAt
            )
            waterLogDao.insertWaterLog(entity)
            waterInserted++
        }
        result = result.copy(waterLogsInserted = waterInserted)

        // 6. Conversations + Messages
        var convsInserted = 0
        var msgsInserted = 0
        for (pulled in response.conversations) {
            val existing = pulled.localId?.let { conversationDao.getConversationById(it) }
            val pulledLastMsgAt = parseIsoToEpoch(pulled.lastMessageAt)

            val entity = ConversationEntity(
                id = existing?.id ?: 0L,
                startedAt = parseIsoToEpoch(pulled.startedAt),
                lastMessageAt = pulledLastMsgAt
            )
            val upsertRes = conversationDao.upsertConversation(entity)
            val targetConvId = if (existing != null && existing.id > 0L) existing.id else upsertRes
            if (existing == null) convsInserted++

            val pulledMsgs = response.messages.filter { it.conversationId == pulled.id }
            for (msg in pulledMsgs) {
                val existingMsg = msg.localId?.let { messageDao.getMessageById(it) }
                if (existingMsg == null) {
                    val msgEntity = MessageEntity(
                        id = 0L,
                        conversationId = targetConvId,
                        role = msg.role,
                        text = msg.textContent,
                        structuredDataJson = msg.structuredData?.toString(),
                        createdAt = parseIsoToEpoch(msg.createdAt)
                    )
                    messageDao.insertMessage(msgEntity)
                    msgsInserted++
                }
            }
        }
        result = result.copy(conversationsInserted = convsInserted, messagesInserted = msgsInserted)

        result
    }

    // -------------------------------------------------------
    // Full sync: push then pull
    // -------------------------------------------------------
    override suspend fun fullSync(): Result<SyncResult> = runCatching {
        val pushResult = pushAll().getOrThrow()
        val lastSync = getLastSyncTimestamp()
        val pullResult = pullSince(lastSync).getOrThrow()
        val now = System.currentTimeMillis()
        setLastSyncTimestamp(now)

        SyncResult(
            push = pushResult,
            pull = pullResult,
            syncedAt = now
        )
    }

    override fun getLastSyncTimestamp(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    override fun setLastSyncTimestamp(epochMs: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, epochMs).apply()
    }

    // -------------------------------------------------------
    // Entity → Push DTO mappers
    // -------------------------------------------------------
    private fun UserProfileEntity.toSyncDto() = SyncUserProfileDto(
        email = email,
        goal = goal,
        units = units,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        goalWeightKg = goalWeightKg,
        activityLevel = activityLevel,
        dietTags = dietTags,
        allergies = allergies,
        targetsSource = targetsSource,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun NutritionGoalEntity.toSyncDto() = SyncNutritionGoalDto(
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

    private fun MealLogEntity.toSyncDto() = SyncMealLogDto(
        id = id,
        date = date,
        mealType = mealType,
        totalCalories = totalCalories,
        loggedAt = loggedAt,
        source = source,
        synced = synced
    )

    private fun com.kcalgrindai.app.data.local.entity.FoodLogItemEntity.toSyncDto() = SyncFoodLogItemDto(
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
        source = source,
        confidence = confidence,
        confirmed = confirmed
    )

    private fun WeightEntryEntity.toSyncDto() = SyncWeightEntryDto(
        id = id,
        weightKg = weightKg,
        date = date,
        note = note,
        loggedAt = loggedAt
    )

    private fun WaterLogEntity.toSyncDto() = SyncWaterLogDto(
        id = id,
        date = date,
        amountMl = amountMl,
        loggedAt = loggedAt
    )

    private fun com.kcalgrindai.app.data.local.entity.AIAnalysisEntity.toSyncDto() = SyncAIAnalysisDto(
        id = id,
        inputType = inputType,
        rawInputRef = rawInputRef,
        resultJson = resultJson,
        overallConfidence = overallConfidence,
        createdAt = createdAt
    )

    private fun ConversationEntity.toSyncDto() = SyncConversationDto(
        id = id,
        startedAt = startedAt,
        lastMessageAt = lastMessageAt
    )

    private fun MessageEntity.toSyncDto() = SyncMessageDto(
        id = id,
        conversationId = conversationId,
        role = role,
        text = text,
        structuredDataJson = structuredDataJson,
        createdAt = createdAt
    )

    private fun parseIsoToEpoch(isoStr: String): Long {
        return try {
            isoFormat.parse(isoStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    companion object {
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
}
