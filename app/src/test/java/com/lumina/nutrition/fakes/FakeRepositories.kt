package com.lumina.nutrition.fakes

import com.lumina.nutrition.domain.model.AIAnalysisRecord
import com.lumina.nutrition.domain.model.AIAnalysisResult
import com.lumina.nutrition.domain.model.AIChatResponse
import com.lumina.nutrition.domain.model.AIFoodItem
import com.lumina.nutrition.domain.model.AIMacros
import com.lumina.nutrition.domain.model.ChatMessage
import com.lumina.nutrition.domain.model.Conversation
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.repository.AIAnalysisRepository
import com.lumina.nutrition.domain.repository.AICoachRepository
import com.lumina.nutrition.domain.repository.NutritionGoalRepository
import com.lumina.nutrition.domain.repository.UserProfileRepository
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

class FakeFoodRepository : com.lumina.nutrition.domain.repository.FoodRepository {
    private val foods = mutableListOf<com.lumina.nutrition.domain.model.FoodItem>()
    var shouldFailOnline: Boolean = false

    override suspend fun saveFood(food: com.lumina.nutrition.domain.model.FoodItem): Long {
        val id = if (food.id == 0L) (foods.size + 1).toLong() else food.id
        val item = food.copy(id = id)
        foods.removeAll { it.id == id }
        foods.add(item)
        return id
    }

    override suspend fun saveFoods(foodsList: List<com.lumina.nutrition.domain.model.FoodItem>): List<Long> {
        return foodsList.map { saveFood(it) }
    }

    override suspend fun getFoodById(id: Long): com.lumina.nutrition.domain.model.FoodItem? {
        return foods.find { it.id == id }
    }

    override suspend fun getFoodByBarcode(barcode: String): com.lumina.nutrition.domain.model.FoodItem? {
        return foods.find { it.barcodeUpc == barcode }
    }

    override suspend fun searchFoods(query: String, limit: Int): List<com.lumina.nutrition.domain.model.FoodItem> {
        return foods.filter { it.name.contains(query, ignoreCase = true) }.take(limit)
    }

    override suspend fun searchFoodsOnline(query: String): Result<List<com.lumina.nutrition.domain.model.FoodItem>> {
        if (shouldFailOnline) {
            return Result.failure(java.io.IOException("Network unavailable"))
        }
        val matches = foods.filter { it.name.contains(query, ignoreCase = true) }
        return Result.success(matches)
    }

    override suspend fun lookupBarcode(barcode: String): Result<com.lumina.nutrition.domain.model.FoodItem?> {
        if (shouldFailOnline) {
            return Result.failure(java.io.IOException("Network unavailable"))
        }
        val found = foods.find { it.barcodeUpc == barcode }
        return Result.success(found)
    }

    override fun observeFoodsByName(query: String): Flow<List<com.lumina.nutrition.domain.model.FoodItem>> {
        return MutableStateFlow(foods.filter { it.name.contains(query, ignoreCase = true) })
    }

    override fun observeUserCreatedFoods(): Flow<List<com.lumina.nutrition.domain.model.FoodItem>> {
        return MutableStateFlow(foods.filter { it.isUserCreated })
    }

    override fun observeAllFoods(): Flow<List<com.lumina.nutrition.domain.model.FoodItem>> {
        return MutableStateFlow(foods)
    }

    override suspend fun deleteFood(food: com.lumina.nutrition.domain.model.FoodItem) {
        foods.removeIf { it.id == food.id }
    }

    override suspend fun deleteFoodById(id: Long) {
        foods.removeIf { it.id == id }
    }
}

class FakeMealLogRepository : com.lumina.nutrition.domain.repository.MealLogRepository {
    private val meals = mutableListOf<com.lumina.nutrition.domain.model.MealLog>()
    private val items = mutableListOf<com.lumina.nutrition.domain.model.FoodLogItem>()
    private val updateTrigger = MutableStateFlow(0L)

    private fun notifyUpdate() {
        updateTrigger.value = System.currentTimeMillis()
    }

    override suspend fun saveMeal(meal: com.lumina.nutrition.domain.model.MealLog, itemsList: List<com.lumina.nutrition.domain.model.FoodLogItem>): Long {
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

    override suspend fun updateMeal(meal: com.lumina.nutrition.domain.model.MealLog) {
        meals.removeAll { it.id == meal.id }
        meals.add(meal)
        notifyUpdate()
    }

    override suspend fun deleteMeal(mealId: Long) {
        meals.removeIf { it.id == mealId }
        items.removeIf { it.mealLogId == mealId }
        notifyUpdate()
    }

    override suspend fun getMealById(mealId: Long): com.lumina.nutrition.domain.model.MealWithFoodItems? {
        val meal = meals.find { it.id == mealId } ?: return null
        val mealItems = items.filter { it.mealLogId == mealId }
        return com.lumina.nutrition.domain.model.MealWithFoodItems(meal, mealItems)
    }

    fun getMealsWithItemsByDate(date: String): List<com.lumina.nutrition.domain.model.MealWithFoodItems> {
        return meals.filter { it.date == date }.map { meal ->
            com.lumina.nutrition.domain.model.MealWithFoodItems(
                meal = meal,
                items = items.filter { it.mealLogId == meal.id }
            )
        }
    }

    override fun observeMealsWithItemsByDate(date: String): Flow<List<com.lumina.nutrition.domain.model.MealWithFoodItems>> {
        return updateTrigger.map {
            meals.filter { it.date == date }.map { meal ->
                com.lumina.nutrition.domain.model.MealWithFoodItems(
                    meal = meal,
                    items = items.filter { it.mealLogId == meal.id }
                )
            }
        }
    }

    override fun observeMealsWithItemsInRange(startDate: String, endDate: String): Flow<List<com.lumina.nutrition.domain.model.MealWithFoodItems>> {
        return updateTrigger.map {
            meals.filter { it.date in startDate..endDate }.map { meal ->
                com.lumina.nutrition.domain.model.MealWithFoodItems(
                    meal = meal,
                    items = items.filter { it.mealLogId == meal.id }
                )
            }
        }
    }

    override suspend fun saveFoodLogItem(item: com.lumina.nutrition.domain.model.FoodLogItem): Long {
        val id = if (item.id == 0L) (items.size + 1).toLong() else item.id
        val newItem = item.copy(id = id)
        items.removeAll { it.id == id }
        items.add(newItem)
        notifyUpdate()
        return id
    }

    override suspend fun updateFoodLogItem(item: com.lumina.nutrition.domain.model.FoodLogItem) {
        items.removeAll { it.id == item.id }
        items.add(item)
        notifyUpdate()
    }

    override suspend fun deleteFoodLogItem(itemId: Long) {
        items.removeIf { it.id == itemId }
        notifyUpdate()
    }

    override fun observeItemsForMeal(mealId: Long): Flow<List<com.lumina.nutrition.domain.model.FoodLogItem>> {
        return updateTrigger.map {
            items.filter { it.mealLogId == mealId }
        }
    }

    override suspend fun getUnsyncedMeals(): List<com.lumina.nutrition.domain.model.MealLog> {
        return meals.filter { !it.synced }
    }
}

class FakeAIAnalysisRepository : AIAnalysisRepository {
    private val records = mutableListOf<AIAnalysisRecord>()
    var shouldFail: Boolean = false

    override suspend fun analyzePhoto(
        imageBase64: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> {
        if (shouldFail) return Result.failure(java.io.IOException("No connection"))
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

class FakeAICoachRepository : AICoachRepository {
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
        recentTrends: com.lumina.nutrition.data.remote.dto.RecentTrendsDto?
    ): Result<AIChatResponse> {
        if (shouldFail) return Result.failure(java.io.IOException("AI Coach unavailable"))
        saveMessage(ChatMessage(0L, conversationId, com.lumina.nutrition.domain.model.MessageRole.USER, userContent, null, System.currentTimeMillis()))
        
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
        
        saveMessage(ChatMessage(0L, conversationId, com.lumina.nutrition.domain.model.MessageRole.ASSISTANT, reply, suggestedAction, System.currentTimeMillis()))
        return Result.success(AIChatResponse(reply = reply, suggestedAction = suggestedAction))
    }
}

class FakeWaterLogRepository : com.lumina.nutrition.domain.repository.WaterLogRepository {
    val logs = mutableListOf<com.lumina.nutrition.domain.model.WaterLog>()
    private var counter = 0L
    private val trigger = MutableStateFlow(0L)

    override suspend fun saveWaterLog(entry: com.lumina.nutrition.domain.model.WaterLog): Long {
        val id = (logs.size + 1).toLong()
        logs.add(entry.copy(id = id))
        trigger.value = ++counter
        return id
    }

    override suspend fun logWater(amountMl: Int, date: String): Long {
        return saveWaterLog(
            com.lumina.nutrition.domain.model.WaterLog(
                id = 0L,
                date = date,
                amountMl = amountMl,
                loggedAt = System.currentTimeMillis()
            )
        )
    }

    override fun observeWaterLogsByDate(date: String): Flow<List<com.lumina.nutrition.domain.model.WaterLog>> {
        return trigger.map { logs.filter { it.date == date } }
    }

    override suspend fun getWaterLogsByDate(date: String): List<com.lumina.nutrition.domain.model.WaterLog> {
        return logs.filter { it.date == date }
    }

    override fun observeWaterLogsInRange(startDate: String, endDate: String): Flow<List<com.lumina.nutrition.domain.model.WaterLog>> {
        return trigger.map { logs.filter { it.date in startDate..endDate } }
    }

    override suspend fun getWaterLogsInRange(startDate: String, endDate: String): List<com.lumina.nutrition.domain.model.WaterLog> {
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

class FakeWeightRepository : com.lumina.nutrition.domain.repository.WeightRepository {
    private val entries = mutableListOf<com.lumina.nutrition.domain.model.WeightEntry>()
    private var counter = 0L
    private val trigger = MutableStateFlow(0L)

    override suspend fun saveWeightEntry(entry: com.lumina.nutrition.domain.model.WeightEntry): Long {
        val id = if (entry.id == 0L) (entries.size + 1).toLong() else entry.id
        val newEntry = entry.copy(id = id)
        entries.removeAll { it.id == id }
        entries.add(newEntry)
        trigger.value = ++counter
        return id
    }

    override suspend fun getWeightEntryById(id: Long): com.lumina.nutrition.domain.model.WeightEntry? {
        return entries.find { it.id == id }
    }

    override suspend fun getLatestWeightEntry(): com.lumina.nutrition.domain.model.WeightEntry? {
        return entries.maxByOrNull { it.loggedAt }
    }

    override fun observeLatestWeightEntry(): Flow<com.lumina.nutrition.domain.model.WeightEntry?> {
        return trigger.map { entries.maxByOrNull { it.loggedAt } }
    }

    override fun observeAllWeightEntries(): Flow<List<com.lumina.nutrition.domain.model.WeightEntry>> {
        return trigger.map { entries.toList() }
    }

    override fun observeWeightEntriesInRange(startDate: String, endDate: String): Flow<List<com.lumina.nutrition.domain.model.WeightEntry>> {
        return trigger.map { entries.filter { it.date in startDate..endDate } }
    }

    override suspend fun deleteWeightEntry(id: Long) {
        entries.removeIf { it.id == id }
        trigger.value = ++counter
    }
}

class FakeAuthRepository : com.lumina.nutrition.domain.repository.AuthRepository {
    private val _authState = MutableStateFlow<com.lumina.nutrition.domain.model.AuthState>(
        com.lumina.nutrition.domain.model.AuthState.Unauthenticated
    )
    override fun observeAuthState(): Flow<com.lumina.nutrition.domain.model.AuthState> = _authState.asStateFlow()
    var currentUser: com.lumina.nutrition.domain.model.AuthUser? = null

    override suspend fun getCurrentUser(): com.lumina.nutrition.domain.model.AuthUser? = currentUser

    var shouldFail: Boolean = false
    var failureMessage: String = "Auth operation failed"

    override suspend fun signInWithEmail(email: String, password: String): Result<com.lumina.nutrition.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.lumina.nutrition.domain.model.AuthUser(
            uid = "fake-uid-123",
            email = email,
            displayName = email.substringBefore("@"),
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.lumina.nutrition.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<com.lumina.nutrition.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.lumina.nutrition.domain.model.AuthUser(
            uid = "fake-uid-123",
            email = email,
            displayName = email.substringBefore("@"),
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.lumina.nutrition.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<com.lumina.nutrition.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.lumina.nutrition.domain.model.AuthUser(
            uid = "google-uid-456",
            email = "user@google.com",
            displayName = "Google User",
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.lumina.nutrition.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInAsGuest(): Result<com.lumina.nutrition.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.lumina.nutrition.domain.model.AuthUser(
            uid = "guest-uid-789",
            email = null,
            displayName = null,
            isAnonymous = true
        )
        currentUser = user
        _authState.value = com.lumina.nutrition.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun linkWithEmail(email: String, password: String): Result<com.lumina.nutrition.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.lumina.nutrition.domain.model.AuthUser(
            uid = currentUser?.uid ?: "guest-uid-789",
            email = email,
            displayName = email.substringBefore("@"),
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.lumina.nutrition.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun linkWithGoogle(idToken: String): Result<com.lumina.nutrition.domain.model.AuthUser> {
        if (shouldFail) return Result.failure(Exception(failureMessage))
        val user = com.lumina.nutrition.domain.model.AuthUser(
            uid = currentUser?.uid ?: "guest-uid-789",
            email = "linked@google.com",
            displayName = "Linked User",
            isAnonymous = false
        )
        currentUser = user
        _authState.value = com.lumina.nutrition.domain.model.AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return if (currentUser != null) "fake-id-token-xyz" else null
    }

    override suspend fun signOut() {
        currentUser = null
        _authState.value = com.lumina.nutrition.domain.model.AuthState.Unauthenticated
    }
}
