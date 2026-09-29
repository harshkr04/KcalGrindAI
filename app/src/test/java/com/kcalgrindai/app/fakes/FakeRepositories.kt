package com.kcalgrindai.app.fakes

import com.kcalgrindai.app.domain.model.AIAnalysisRecord
import com.kcalgrindai.app.domain.model.AIAnalysisResult
import com.kcalgrindai.app.domain.model.AIChatResponse
import com.kcalgrindai.app.domain.model.AIFoodItem
import com.kcalgrindai.app.domain.model.AIMacros
import com.kcalgrindai.app.domain.model.ChatMessage
import com.kcalgrindai.app.domain.model.Conversation
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.repository.AIAnalysisRepository
import com.kcalgrindai.app.domain.repository.AICoachRepository
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeUserProfileRepository : UserProfileRepository {
    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    private var goal: NutritionGoal? = null

    fun setGoal(g: NutritionGoal?) {
        goal = g
    }

    override suspend fun saveProfile(profile: UserProfile): Long {
        val id = if (profile.id == 0L) 1L else profile.id
        val saved = profile.copy(id = id)
        profileFlow.value = saved
        return id
    }

    override suspend fun saveProfileAndGoal(profile: UserProfile, goal: NutritionGoal): Long {
        val id = if (profile.id == 0L) 1L else profile.id
        val saved = profile.copy(id = id)
        profileFlow.value = saved
        this.goal = goal.copy(id = 1L, userId = id)
        return id
    }

    override suspend fun getProfile(): UserProfile? = profileFlow.value

    override suspend fun getProfileByFirebaseUid(firebaseUid: String): UserProfile? {
        val current = profileFlow.value ?: return null
        return if (current.firebaseUid == firebaseUid) current else null
    }

    override suspend fun updateAuthDetails(firebaseUid: String?, email: String?) {
        val current = profileFlow.value ?: return
        profileFlow.value = current.copy(firebaseUid = firebaseUid, email = email)
    }

    override fun observeProfile(): Flow<UserProfile?> = profileFlow.asStateFlow()

    override fun observeProfileWithGoal(): Flow<Pair<UserProfile, NutritionGoal?>?> {
        return profileFlow.map { profile ->
            profile?.let { Pair(it, goal) }
        }
    }

    override suspend fun deleteProfile() {
        profileFlow.value = null
        goal = null
    }
}

class FakeNutritionGoalRepository : NutritionGoalRepository {
    private val goalFlow = MutableStateFlow<NutritionGoal?>(null)

    override suspend fun saveGoal(goal: NutritionGoal): Long {
        goalFlow.value = goal
        return goal.id
    }

    override suspend fun getGoalByUserId(userId: Long): NutritionGoal? = goalFlow.value

    override suspend fun getLatestGoal(): NutritionGoal? = goalFlow.value

    override fun observeGoalByUserId(userId: Long): Flow<NutritionGoal?> = goalFlow.asStateFlow()

    override fun observeLatestGoal(): Flow<NutritionGoal?> = goalFlow.asStateFlow()

    override suspend fun deleteGoalByUserId(userId: Long) {
        goalFlow.value = null
    }
}

class FakeFoodRepository : com.kcalgrindai.app.domain.repository.FoodRepository {
    private val foods = mutableListOf<com.kcalgrindai.app.domain.model.FoodItem>()
    var shouldFailOnline: Boolean = false

    override suspend fun saveFood(food: com.kcalgrindai.app.domain.model.FoodItem): Long {
        val id = if (food.id == 0L) (foods.size + 1).toLong() else food.id
        val item = food.copy(id = id)
        foods.removeAll { it.id == id }
        foods.add(item)
        return id
    }

    override suspend fun saveFoods(foodsList: List<com.kcalgrindai.app.domain.model.FoodItem>): List<Long> {
        return foodsList.map { saveFood(it) }
    }

    override suspend fun getFoodById(id: Long): com.kcalgrindai.app.domain.model.FoodItem? {
        return foods.find { it.id == id }
    }

    override suspend fun getFoodByBarcode(barcode: String): com.kcalgrindai.app.domain.model.FoodItem? {
        return foods.find { it.barcodeUpc == barcode }
    }

    override suspend fun searchFoods(query: String, limit: Int): List<com.kcalgrindai.app.domain.model.FoodItem> {
        return foods.filter { it.name.contains(query, ignoreCase = true) }.take(limit)
    }

    override suspend fun searchFoodsOnline(query: String): Result<List<com.kcalgrindai.app.domain.model.FoodItem>> {
        if (shouldFailOnline) {
            return Result.failure(java.io.IOException("Network unavailable"))
        }
        val matches = foods.filter { it.name.contains(query, ignoreCase = true) }
        return Result.success(matches)
    }

    override suspend fun lookupBarcode(barcode: String): Result<com.kcalgrindai.app.domain.model.FoodItem?> {
        if (shouldFailOnline) {
            return Result.failure(java.io.IOException("Network unavailable"))
        }
        val found = foods.find { it.barcodeUpc == barcode }
        return Result.success(found)
    }

    override fun observeFoodsByName(query: String): Flow<List<com.kcalgrindai.app.domain.model.FoodItem>> {
        return MutableStateFlow(foods.filter { it.name.contains(query, ignoreCase = true) })
    }

    override fun observeUserCreatedFoods(): Flow<List<com.kcalgrindai.app.domain.model.FoodItem>> {
        return MutableStateFlow(foods.filter { it.isUserCreated })
    }

    override fun observeAllFoods(): Flow<List<com.kcalgrindai.app.domain.model.FoodItem>> {
        return MutableStateFlow(foods)
    }

    override suspend fun deleteFood(food: com.kcalgrindai.app.domain.model.FoodItem) {
        foods.removeIf { it.id == food.id }
    }

    override suspend fun deleteFoodById(id: Long) {
        foods.removeIf { it.id == id }
    }
}

class FakeMealLogRepository : com.kcalgrindai.app.domain.repository.MealLogRepository {
    private val meals = mutableListOf<com.kcalgrindai.app.domain.model.MealLog>()
    private val items = mutableListOf<com.kcalgrindai.app.domain.model.FoodLogItem>()
    private val updateTrigger = MutableStateFlow(0L)

    private fun notifyUpdate() {
        updateTrigger.value += 1
    }

    override suspend fun saveMeal(meal: com.kcalgrindai.app.domain.model.MealLog, itemsList: List<com.kcalgrindai.app.domain.model.FoodLogItem>): Long {
        val id = if (meal.id == 0L) (meals.size + 1).toLong() else meal.id
        val item = meal.copy(id = id)
        meals.removeAll { it.id == id }
        meals.add(item)
        itemsList.forEach { logItem ->
            saveFoodLogItem(logItem.copy(mealLogId = id))
        }
        notifyUpdate()
        return id
    }

    override suspend fun updateMeal(meal: com.kcalgrindai.app.domain.model.MealLog) {
        meals.removeAll { it.id == meal.id }
        meals.add(meal)
        notifyUpdate()
    }

    override suspend fun deleteMeal(mealId: Long) {
        meals.removeIf { it.id == mealId }
        items.removeIf { it.mealLogId == mealId }
        notifyUpdate()
    }

    override suspend fun getMealById(mealId: Long): com.kcalgrindai.app.domain.model.MealWithFoodItems? {
        val meal = meals.find { it.id == mealId } ?: return null
        val mealItems = items.filter { it.mealLogId == mealId }
        return com.kcalgrindai.app.domain.model.MealWithFoodItems(meal, mealItems)
    }

    fun getMealsWithItemsByDate(date: String): List<com.kcalgrindai.app.domain.model.MealWithFoodItems> {
        return meals.filter { it.date == date }.map { meal ->
            com.kcalgrindai.app.domain.model.MealWithFoodItems(
                meal = meal,
                items = items.filter { it.mealLogId == meal.id }
            )
        }
    }

    override fun observeMealsWithItemsByDate(date: String): Flow<List<com.kcalgrindai.app.domain.model.MealWithFoodItems>> {
        return updateTrigger.map {
            meals.filter { it.date == date }.map { meal ->
                com.kcalgrindai.app.domain.model.MealWithFoodItems(
                    meal = meal,
                    items = items.filter { it.mealLogId == meal.id }
                )
            }
        }
    }

    override fun observeMealsWithItemsInRange(startDate: String, endDate: String): Flow<List<com.kcalgrindai.app.domain.model.MealWithFoodItems>> {
        return updateTrigger.map {
            meals.filter { it.date in startDate..endDate }.map { meal ->
                com.kcalgrindai.app.domain.model.MealWithFoodItems(
                    meal = meal,
                    items = items.filter { it.mealLogId == meal.id }
                )
            }
        }
    }

    override suspend fun saveFoodLogItem(item: com.kcalgrindai.app.domain.model.FoodLogItem): Long {
        val id = if (item.id == 0L) (items.size + 1).toLong() else item.id
        val newItem = item.copy(id = id)
        items.removeAll { it.id == id }
        items.add(newItem)
        notifyUpdate()
        return id
    }

    override suspend fun updateFoodLogItem(item: com.kcalgrindai.app.domain.model.FoodLogItem) {
        items.removeAll { it.id == item.id }
        items.add(item)
        notifyUpdate()
    }

    override suspend fun deleteFoodLogItem(itemId: Long) {
        items.removeIf { it.id == itemId }
        notifyUpdate()
    }

    override fun observeItemsForMeal(mealId: Long): Flow<List<com.kcalgrindai.app.domain.model.FoodLogItem>> {
        return updateTrigger.map {
            items.filter { it.mealLogId == mealId }
        }
    }

    override suspend fun getUnsyncedMeals(): List<com.kcalgrindai.app.domain.model.MealLog> {
        return meals.filter { !it.synced }
    }

    override fun observeStreakDays(): Flow<Int> {
        return updateTrigger.map {
            getStreakDays()
        }
    }

    override suspend fun getStreakDays(): Int {
        val dates = meals.map { it.date }.distinct()
        return com.kcalgrindai.app.data.repository.MealLogRepositoryImpl.calculateStreak(dates)
    }
}

class FakeAIAnalysisRepository : AIAnalysisRepository {
    private val records = mutableListOf<AIAnalysisRecord>()
    var shouldFail: Boolean = false
    var mockPhotoResult: AIAnalysisResult? = null
    var mockPhotoFileFailure: Throwable? = null

    override suspend fun analyzePhoto(
        imageBase64: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> {
        if (shouldFail) return Result.failure(java.io.IOException("No connection"))
        mockPhotoResult?.let { return Result.success(it) }
        return Result.success(
            AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Avocado Toast with Egg", 180.0, 320, AIMacros(14.0, 24.0, 18.0), 0.94)
                ),
                overallConfidence = 0.94,
                inputType = "photo"
            )
        )
    }

    override suspend fun analyzePhotoFile(
        photoPath: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> {
        mockPhotoFileFailure?.let { return Result.failure(it) }
        if (shouldFail) return Result.failure(java.io.IOException("No connection"))
        mockPhotoResult?.let { return Result.success(it) }
        return Result.success(
            AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Avocado Toast with Egg", 180.0, 320, AIMacros(14.0, 24.0, 18.0), 0.94)
                ),
                overallConfidence = 0.94,
                inputType = "photo"
            )
        )
    }

    override suspend fun analyzeText(
        text: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> {
        if (shouldFail) return Result.failure(java.io.IOException("No connection"))
        return Result.success(
            AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("2 Eggs, whole", 100.0, 144, AIMacros(12.6, 0.8, 9.9), 0.95),
                    AIFoodItem("Whole Wheat Toast", 60.0, 160, AIMacros(6.0, 28.0, 2.0), 0.92)
                ),
                overallConfidence = 0.935,
                rawQuery = text,
                inputType = "text"
            )
        )
    }

    override suspend fun transcribeAndAnalyze(
        transcript: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> {
        if (shouldFail) return Result.failure(java.io.IOException("No connection"))
        return Result.success(
            AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Greek Yogurt Bowl", 200.0, 180, AIMacros(18.0, 12.0, 5.0), 0.91)
                ),
                overallConfidence = 0.91,
                rawQuery = transcript,
                inputType = "voice"
            )
        )
    }

    override suspend fun saveAnalysis(analysis: AIAnalysisRecord): Long {
        val id = (records.size + 1).toLong()
        records.add(analysis.copy(id = id))
        return id
    }

    override suspend fun getAnalysisById(id: Long): AIAnalysisRecord? = records.find { it.id == id }

    override fun observeRecentAnalyses(limit: Int): Flow<List<AIAnalysisRecord>> = MutableStateFlow(records.take(limit))

    override suspend fun deleteOlderThan(cutoffTimestamp: Long): Int = 0

    override suspend fun clearAll() {
        records.clear()
    }
}

class FakeAICoachRepository(
    var usageRepository: com.kcalgrindai.app.domain.repository.AICoachUsageRepository? = null
) : AICoachRepository {
    private val conversations = mutableListOf<Conversation>()
    private val messages = mutableListOf<ChatMessage>()
    var shouldFail: Boolean = false

    override suspend fun createConversation(startedAt: Long): Long {
        val id = (conversations.size + 1).toLong()
        conversations.add(Conversation(id, startedAt, startedAt))
        return id
    }

    override suspend fun updateConversationLastMessage(conversationId: Long, lastMessageAt: Long) {}

    override suspend fun getConversationById(id: Long): Conversation? = conversations.find { it.id == id }

    override fun observeAllConversations(): Flow<List<Conversation>> = MutableStateFlow(conversations)

    override fun observeConversationWithMessages(id: Long): Flow<Conversation?> = MutableStateFlow(conversations.find { it.id == id })

    override fun observeLatestConversation(): Flow<Conversation?> = MutableStateFlow(conversations.lastOrNull())

    override suspend fun getLatestConversation(): Conversation? = conversations.lastOrNull()

    override suspend fun deleteConversation(id: Long) {
        conversations.removeIf { it.id == id }
    }

    override suspend fun saveMessage(message: ChatMessage): Long {
        val id = (messages.size + 1).toLong()
        messages.add(message.copy(id = id))
        return id
    }

    override suspend fun getMessagesForConversation(conversationId: Long): List<ChatMessage> =
        messages.filter { it.conversationId == conversationId }

    override fun observeMessagesForConversation(conversationId: Long): Flow<List<ChatMessage>> =
        MutableStateFlow(messages.filter { it.conversationId == conversationId })

    override suspend fun sendChatMessage(
        conversationId: Long,
        userContent: String,
        remainingCalories: Int?,
        consumedCalories: Int?,
        targetCalories: Int?,
        goal: String?,
        recentTrends: com.kcalgrindai.app.data.remote.dto.RecentTrendsDto?
    ): Result<AIChatResponse> {
        try {
            usageRepository?.checkQuota()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (shouldFail) return Result.failure(java.io.IOException("AI Coach unavailable"))
        saveMessage(ChatMessage(0L, conversationId, com.kcalgrindai.app.domain.model.MessageRole.USER, userContent, null, System.currentTimeMillis()))
        
        var suggestedAction: String? = null
        val reply = when {
            userContent.contains("water", ignoreCase = true) -> {
                suggestedAction = """{"type":"log_water","action":"auto_logged_water","data":{"amountMl":250}}"""
                "I've logged a 250ml glass of water for you!"
            }
            userContent.contains("banana", ignoreCase = true) -> {
                suggestedAction = """{"type":"create_food_log","action":"confirm_food_log","data":{"mealType":"breakfast","items":[{"name":"Banana","estimatedGrams":120.0,"calories":105,"macros":{"protein":1.3,"carbs":27.0,"fat":0.3},"confidence":0.95}]}}"""
                "I've prepared a breakfast entry for 1 Banana (105 kcal). Please confirm to log it."
            }
            userContent.contains("week", ignoreCase = true) || userContent.contains("trend", ignoreCase = true) -> {
                "This week you averaged ${recentTrends?.avgCalories ?: 1850} kcal per day across ${recentTrends?.daysTracked ?: 5} tracked days."
            }
            else -> "Great question about your $goal goal! You have $remainingCalories kcal left."
        }
        
        saveMessage(ChatMessage(0L, conversationId, com.kcalgrindai.app.domain.model.MessageRole.ASSISTANT, reply, suggestedAction, System.currentTimeMillis()))
        usageRepository?.recordMessageSent()
        return Result.success(AIChatResponse(reply = reply, suggestedAction = suggestedAction))
    }
}

class FakeWaterLogRepository : com.kcalgrindai.app.domain.repository.WaterLogRepository {
    val logs = mutableListOf<com.kcalgrindai.app.domain.model.WaterLog>()
    private var counter = 0L
    private val trigger = MutableStateFlow(0L)

    override suspend fun saveWaterLog(entry: com.kcalgrindai.app.domain.model.WaterLog): Long {
        if (entry.id != 0L) {
            val idx = logs.indexOfFirst { it.id == entry.id }
            if (idx >= 0) {
                logs[idx] = entry
                trigger.value = ++counter
                return entry.id
            }
        }
        val id = (logs.size + 1).toLong()
        logs.add(entry.copy(id = id))
        trigger.value = ++counter
        return id
    }

    override suspend fun logWater(amountMl: Int, date: String): Long {
        return saveWaterLog(
            com.kcalgrindai.app.domain.model.WaterLog(
                id = 0L,
                date = date,
                amountMl = amountMl,
                loggedAt = System.currentTimeMillis()
            )
        )
    }

    override fun observeWaterLogsByDate(date: String): Flow<List<com.kcalgrindai.app.domain.model.WaterLog>> {
        return trigger.map { logs.filter { it.date == date } }
    }

    override suspend fun getWaterLogsByDate(date: String): List<com.kcalgrindai.app.domain.model.WaterLog> {
        return logs.filter { it.date == date }
    }

    override fun observeWaterLogsInRange(startDate: String, endDate: String): Flow<List<com.kcalgrindai.app.domain.model.WaterLog>> {
        return trigger.map { logs.filter { it.date in startDate..endDate } }
    }

    override suspend fun getWaterLogsInRange(startDate: String, endDate: String): List<com.kcalgrindai.app.domain.model.WaterLog> {
        return logs.filter { it.date in startDate..endDate }
    }

    override fun observeTotalWaterByDate(date: String): Flow<Int> {
        return trigger.map { logs.filter { it.date == date }.sumOf { it.amountMl } }
    }

    override suspend fun deleteWaterLog(id: Long) {
        logs.removeIf { it.id == id }
        trigger.value = ++counter
    }
}

class FakeWeightRepository : com.kcalgrindai.app.domain.repository.WeightRepository {
    private val entries = mutableListOf<com.kcalgrindai.app.domain.model.WeightEntry>()
    private var counter = 0L
    private val trigger = MutableStateFlow(0L)

    override suspend fun saveWeightEntry(entry: com.kcalgrindai.app.domain.model.WeightEntry): Long {
        val id = if (entry.id == 0L) (entries.size + 1).toLong() else entry.id
        val newEntry = entry.copy(id = id)
        entries.removeAll { it.id == id }
        entries.add(newEntry)
        trigger.value = ++counter
        return id
    }

    override suspend fun getWeightEntryById(id: Long): com.kcalgrindai.app.domain.model.WeightEntry? {
        return entries.find { it.id == id }
    }

    override suspend fun getLatestWeightEntry(): com.kcalgrindai.app.domain.model.WeightEntry? {
        return entries.maxByOrNull { it.loggedAt }
    }

    override fun observeLatestWeightEntry(): Flow<com.kcalgrindai.app.domain.model.WeightEntry?> {
        return trigger.map { entries.maxByOrNull { it.loggedAt } }
    }

    override fun observeAllWeightEntries(): Flow<List<com.kcalgrindai.app.domain.model.WeightEntry>> {
        return trigger.map { entries.toList() }
    }

    override fun observeWeightEntriesInRange(startDate: String, endDate: String): Flow<List<com.kcalgrindai.app.domain.model.WeightEntry>> {
        return trigger.map { entries.filter { it.date in startDate..endDate } }
    }

    override suspend fun deleteWeightEntry(id: Long) {
        entries.removeIf { it.id == id }
        trigger.value = ++counter
    }
}

class FakeAuthRepository : com.kcalgrindai.app.domain.repository.AuthRepository {
    private val _authState = MutableStateFlow<com.kcalgrindai.app.domain.model.AuthState>(
        com.kcalgrindai.app.domain.model.AuthState.Unauthenticated
    )
    override fun observeAuthState(): Flow<com.kcalgrindai.app.domain.model.AuthState> = _authState.asStateFlow()
    var currentUser: com.kcalgrindai.app.domain.model.AuthUser? = null

    override suspend fun getCurrentUser(): com.kcalgrindai.app.domain.model.AuthUser? = currentUser

    var shouldFail: Boolean = false
    var failureMessage: String = "Auth operation failed"

    override suspend fun signInWithEmail(email: String, password: String): Result<com.kcalgrindai.app.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.kcalgrindai.app.domain.model.AuthUser(
            uid = "fake-uid-123",
            email = email,
            displayName = email.substringBefore("@"),
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.kcalgrindai.app.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<com.kcalgrindai.app.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.kcalgrindai.app.domain.model.AuthUser(
            uid = "fake-uid-123",
            email = email,
            displayName = email.substringBefore("@"),
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.kcalgrindai.app.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<com.kcalgrindai.app.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.kcalgrindai.app.domain.model.AuthUser(
            uid = "google-uid-456",
            email = "user@google.com",
            displayName = "Google User",
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.kcalgrindai.app.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInAsGuest(): Result<com.kcalgrindai.app.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.kcalgrindai.app.domain.model.AuthUser(
            uid = "guest-uid-789",
            email = null,
            displayName = null,
            isAnonymous = true
        )
        currentUser = user
        _authState.value = com.kcalgrindai.app.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun linkWithEmail(email: String, password: String): Result<com.kcalgrindai.app.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.kcalgrindai.app.domain.model.AuthUser(
            uid = currentUser?.uid ?: "guest-uid-789",
            email = email,
            displayName = email.substringBefore("@"),
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.kcalgrindai.app.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun linkWithGoogle(idToken: String): Result<com.kcalgrindai.app.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.kcalgrindai.app.domain.model.AuthUser(
            uid = currentUser?.uid ?: "guest-uid-789",
            email = "linked@google.com",
            displayName = "Linked User",
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.kcalgrindai.app.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return if (currentUser != null) "fake-id-token-xyz" else null
    }

    override suspend fun signOut() {
        currentUser = null
        _authState.value = com.kcalgrindai.app.domain.model.AuthState.Unauthenticated
    }
}

class FakeRecipeRepository : com.kcalgrindai.app.domain.repository.RecipeRepository {
    private val recipesFlow = MutableStateFlow<List<com.kcalgrindai.app.domain.model.Recipe>>(emptyList())

    fun setRecipes(list: List<com.kcalgrindai.app.domain.model.Recipe>) {
        recipesFlow.value = list
    }

    override fun observeAllRecipes(): Flow<List<com.kcalgrindai.app.domain.model.Recipe>> = recipesFlow.asStateFlow()

    override fun observeFavoriteRecipes(): Flow<List<com.kcalgrindai.app.domain.model.Recipe>> =
        recipesFlow.map { it.filter { r -> r.isFavorite } }

    override fun observeRecipeById(recipeId: String): Flow<com.kcalgrindai.app.domain.model.Recipe?> =
        recipesFlow.map { it.find { r -> r.id == recipeId } }

    override suspend fun getRecipeById(recipeId: String): com.kcalgrindai.app.domain.model.Recipe? =
        recipesFlow.value.find { it.id == recipeId }

    override suspend fun toggleFavorite(recipeId: String) {
        val current = recipesFlow.value
        recipesFlow.value = current.map {
            if (it.id == recipeId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    override suspend fun logRecipeToDiary(recipe: com.kcalgrindai.app.domain.model.Recipe, targetDate: java.time.LocalDate): Long = 1L

    override suspend fun seedRecipesIfEmpty() {}
}

class FakeAICoachUsageRepository(
    initialUsed: Int = 0,
    initialIsPro: Boolean = false
) : com.kcalgrindai.app.domain.repository.AICoachUsageRepository {
    private val _usageFlow = MutableStateFlow(
        com.kcalgrindai.app.domain.model.AICoachUsage(
            usedToday = initialUsed,
            dailyLimit = if (initialIsPro) 50 else 5,
            isPro = initialIsPro,
            lastResetDate = java.time.LocalDate.now().toString()
        )
    )

    override fun observeUsage(): Flow<com.kcalgrindai.app.domain.model.AICoachUsage> = _usageFlow.asStateFlow()

    override suspend fun getUsage(): com.kcalgrindai.app.domain.model.AICoachUsage = _usageFlow.value

    override suspend fun checkQuota(): Boolean {
        val current = _usageFlow.value
        if (current.isLimitReached) {
            val limitMsg = if (current.isPro) {
                "You've reached today's Pro AI Coach limit."
            } else {
                "You've reached today's free AI Coach limit. Your messages reset tomorrow."
            }
            throw com.kcalgrindai.app.domain.model.AICoachQuotaExceededException(limitMsg)
        }
        return true
    }

    override suspend fun recordMessageSent() {
        val current = _usageFlow.value
        _usageFlow.value = current.copy(usedToday = current.usedToday + 1)
    }

    override suspend fun resetQuotaForNewDay() {
        val current = _usageFlow.value
        _usageFlow.value = current.copy(
            usedToday = 0,
            lastResetDate = java.time.LocalDate.now().toString()
        )
    }

    override suspend fun setPro(isPro: Boolean) {
        val current = _usageFlow.value
        _usageFlow.value = current.copy(
            isPro = isPro,
            dailyLimit = if (isPro) 50 else 5
        )
    }

    override suspend fun resetForTesting(usedCount: Int, date: String?) {
        val current = _usageFlow.value
        _usageFlow.value = current.copy(
            usedToday = usedCount,
            lastResetDate = date ?: java.time.LocalDate.now().toString()
        )
    }
}
