package com.kcalgrindai.app.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.data.health.HealthConnectManager
import com.kcalgrindai.app.data.local.dao.WeightEntryDao
import com.kcalgrindai.app.data.local.entity.WeightEntryEntity
import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.SyncRepository
import com.kcalgrindai.app.domain.repository.SyncResult
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(
        val profile: UserProfile,
        val goal: NutritionGoal?,
        val streakDays: Int = 0,
        val isPro: Boolean = false,
        val avatarUrl: String? = null
    ) : ProfileUiState
    data object Empty : ProfileUiState
}

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Success(val result: SyncResult) : SyncState
    data class Error(val message: String) : SyncState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val nutritionGoalRepository: NutritionGoalRepository,
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    val healthConnectManager: HealthConnectManager? = null,
    private val weightEntryDao: WeightEntryDao? = null,
    private val syncRepository: SyncRepository? = null,
    private val authRepository: AuthRepository? = null,
    private val mealLogRepository: MealLogRepository? = null,
    private val billingRepository: com.kcalgrindai.app.domain.repository.BillingRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val notificationsEnabled: StateFlow<Boolean> = userPreferencesRepository?.notificationsEnabled
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        ?: MutableStateFlow(true).asStateFlow()

    val darkModeEnabled: StateFlow<Boolean> = userPreferencesRepository?.darkModeEnabled
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        ?: MutableStateFlow(false).asStateFlow()

    private val _hasHealthConnectPermission = MutableStateFlow(false)
    val hasHealthConnectPermission: StateFlow<Boolean> = _hasHealthConnectPermission.asStateFlow()

    /** Epoch millis of the last successful sync, or 0 if never synced. */
    val lastSyncTimestamp: Long
        get() = syncRepository?.getLastSyncTimestamp() ?: 0L

    init {
        loadProfile()
        checkHealthConnectPermission()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val streak = mealLogRepository?.getStreakDays() ?: 0
            kotlinx.coroutines.flow.combine(
                userProfileRepository.observeProfileWithGoal(),
                billingRepository?.observeIsPro() ?: kotlinx.coroutines.flow.flowOf(false),
                userPreferencesRepository?.selectedAvatar ?: kotlinx.coroutines.flow.flowOf(null)
            ) { pair, isPro, avatar ->
                if (pair != null) {
                    ProfileUiState.Content(
                        profile = pair.first,
                        goal = pair.second,
                        streakDays = streak,
                        isPro = isPro,
                        avatarUrl = avatar
                    )
                } else {
                    ProfileUiState.Empty
                }
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    fun checkHealthConnectPermission() {
        viewModelScope.launch {
            _hasHealthConnectPermission.value = healthConnectManager?.hasPermissions() ?: false
        }
    }

    val requiredHealthConnectPermissions: Set<String>
        get() = healthConnectManager?.requiredPermissions ?: emptySet()

    fun createPermissionRequestContract(): androidx.activity.result.contract.ActivityResultContract<Set<String>, Set<String>> {
        return healthConnectManager?.createPermissionRequestContract()
            ?: object : androidx.activity.result.contract.ActivityResultContract<Set<String>, Set<String>>() {
                override fun createIntent(context: android.content.Context, input: Set<String>): android.content.Intent =
                    android.content.Intent()
                override fun parseResult(resultCode: Int, intent: android.content.Intent?): Set<String> = emptySet()
            }
    }

    private fun safeLog(msg: String) {
        runCatching { Log.i("ProfileSync", msg) }
    }

    private fun safeLogW(msg: String) {
        runCatching { Log.w("ProfileSync", msg) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            safeLog("Settings: Notifications set to $enabled")
            userPreferencesRepository?.setNotificationsEnabled(enabled)
        }
    }

    val themeMode: StateFlow<com.kcalgrindai.app.domain.model.ThemeMode> = userPreferencesRepository?.themeMode
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.kcalgrindai.app.domain.model.ThemeMode.SYSTEM)
        ?: MutableStateFlow(com.kcalgrindai.app.domain.model.ThemeMode.SYSTEM).asStateFlow()

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            safeLog("Settings: Dark Mode set to $enabled")
            userPreferencesRepository?.setDarkModeEnabled(enabled)
        }
    }

    fun setThemeMode(mode: com.kcalgrindai.app.domain.model.ThemeMode) {
        viewModelScope.launch {
            safeLog("Settings: Theme mode set to $mode")
            userPreferencesRepository?.setThemeMode(mode)
        }
    }

    fun updateHealthProfile(
        firstName: String?,
        heightCm: Double,
        weightKg: Double,
        age: Int,
        activityLevel: ActivityLevel,
        goal: GoalType,
        goalWeightKg: Double
    ) {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            val cleanName = firstName?.trim()?.takeIf { it.isNotBlank() }
            safeLog(
                "Updating health profile in Room: firstName='$cleanName', heightCm=$heightCm, weightKg=$weightKg, age=$age, activity=${activityLevel.id}, goal=${goal.id}, goalWeightKg=$goalWeightKg"
            )

            val updatedProfile = current.copy(
                firstName = cleanName,
                heightCm = heightCm,
                weightKg = weightKg,
                age = age,
                activityLevel = activityLevel,
                goal = goal,
                goalWeightKg = goalWeightKg,
                updatedAt = System.currentTimeMillis()
            )
            userProfileRepository.saveProfile(updatedProfile)

            // If weight changed, log an entry in weight_entries table
            if (weightKg != current.weightKg) {
                val today = LocalDate.now().toString()
                weightEntryDao?.upsertWeightEntry(
                    WeightEntryEntity(
                        weightKg = weightKg,
                        date = today,
                        loggedAt = System.currentTimeMillis(),
                        note = "Updated from Profile"
                    )
                )
                safeLog("Logged weight_entries row: $weightKg kg on $today")
            }

            // Recalculate NutritionGoal targets
            val currentGoal = (_uiState.value as? ProfileUiState.Content)?.goal
            if (currentGoal != null && !currentGoal.isCustom) {
                val calc = NutritionCalculator.calculate(
                    weightKg = weightKg,
                    heightCm = heightCm,
                    age = age,
                    goal = goal,
                    activity = activityLevel,
                    goalWeightKg = goalWeightKg,
                    dietTags = updatedProfile.dietTags
                )
                val updatedGoal = currentGoal.copy(
                    calories = calc.calories,
                    proteinG = calc.proteinG,
                    carbsG = calc.carbsG,
                    fatG = calc.fatG,
                    waterLiters = calc.waterLiters,
                    waterGlasses = calc.waterGlasses,
                    bmr = calc.bmr,
                    tdee = calc.tdee,
                    updatedAt = System.currentTimeMillis()
                )
                nutritionGoalRepository.saveGoal(updatedGoal)
                safeLog(
                    "Recalculated & saved NutritionGoal in Room: calories=${calc.calories}, protein=${calc.proteinG}g, tdee=${calc.tdee}"
                )
            }
        }
    }

    fun updateFirstName(firstName: String?) {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            val cleanName = firstName?.trim()?.takeIf { it.isNotBlank() }
            safeLog("Updating firstName in Room: '$cleanName'")
            val updated = current.copy(
                firstName = cleanName,
                updatedAt = System.currentTimeMillis()
            )
            userProfileRepository.saveProfile(updated)
        }
    }

    fun updateWeight(newWeightKg: Double) {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            safeLog("Updating weight in Room: $newWeightKg kg")
            val updated = current.copy(weightKg = newWeightKg, updatedAt = System.currentTimeMillis())
            userProfileRepository.saveProfile(updated)

            val today = LocalDate.now().toString()
            weightEntryDao?.upsertWeightEntry(
                WeightEntryEntity(
                    weightKg = newWeightKg,
                    date = today,
                    loggedAt = System.currentTimeMillis(),
                    note = "Updated from Profile"
                )
            )

            // Also recalculate goal targets
            val currentGoal = (_uiState.value as? ProfileUiState.Content)?.goal
            if (currentGoal != null && !currentGoal.isCustom) {
                val calc = NutritionCalculator.calculate(
                    weightKg = newWeightKg,
                    heightCm = updated.heightCm,
                    age = updated.age,
                    goal = updated.goal,
                    activity = updated.activityLevel,
                    goalWeightKg = updated.goalWeightKg,
                    dietTags = updated.dietTags
                )
                val updatedGoal = currentGoal.copy(
                    calories = calc.calories,
                    proteinG = calc.proteinG,
                    carbsG = calc.carbsG,
                    fatG = calc.fatG,
                    waterLiters = calc.waterLiters,
                    waterGlasses = calc.waterGlasses,
                    bmr = calc.bmr,
                    tdee = calc.tdee,
                    updatedAt = System.currentTimeMillis()
                )
                nutritionGoalRepository.saveGoal(updatedGoal)
            }
        }
    }

    fun toggleUnitSystem() {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            val newUnits = if (current.units == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
            safeLog("Toggled unit system in Room: ${newUnits.id}")
            val updated = current.copy(units = newUnits, updatedAt = System.currentTimeMillis())
            userProfileRepository.saveProfile(updated)
        }
    }

    fun updateGoal(newGoal: GoalType) {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            safeLog("Updating goal in Room: ${newGoal.id}")
            val updated = current.copy(goal = newGoal, updatedAt = System.currentTimeMillis())
            userProfileRepository.saveProfile(updated)

            val currentGoal = (_uiState.value as? ProfileUiState.Content)?.goal
            if (currentGoal != null && !currentGoal.isCustom) {
                val calc = NutritionCalculator.calculate(
                    weightKg = updated.weightKg,
                    heightCm = updated.heightCm,
                    age = updated.age,
                    goal = newGoal,
                    activity = updated.activityLevel,
                    goalWeightKg = updated.goalWeightKg,
                    dietTags = updated.dietTags
                )
                val updatedGoal = currentGoal.copy(
                    calories = calc.calories,
                    proteinG = calc.proteinG,
                    carbsG = calc.carbsG,
                    fatG = calc.fatG,
                    waterLiters = calc.waterLiters,
                    waterGlasses = calc.waterGlasses,
                    bmr = calc.bmr,
                    tdee = calc.tdee,
                    updatedAt = System.currentTimeMillis()
                )
                nutritionGoalRepository.saveGoal(updatedGoal)
            }
        }
    }

    fun updateAvatarUrl(avatar: String?) {
        viewModelScope.launch {
            safeLog("Updating avatar in preferences: $avatar")
            userPreferencesRepository?.setSelectedAvatar(avatar)
            val current = _uiState.value as? ProfileUiState.Content ?: return@launch
            _uiState.value = current.copy(avatarUrl = avatar)
        }
    }

    /**
     * Manual "Sync now" action (Phase 10, strategy a).
     * Pushes all local Room data to Supabase, then pulls remote changes.
     */
    fun onSyncNow() {
        if (_syncState.value is SyncState.Syncing) return // debounce
        val syncRepo = syncRepository
        if (syncRepo == null) {
            _syncState.value = SyncState.Error("Cloud sync is not configured on this build.")
            return
        }
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            safeLog("Starting cloud sync attempt...")
            syncRepo.fullSync()
                .onSuccess { result ->
                    safeLog("Cloud sync succeeded: $result")
                    _syncState.value = SyncState.Success(result)
                }
                .onFailure { error ->
                    safeLogW("Cloud sync failed cleanly: ${error.javaClass.simpleName} - ${error.message}")
                    val userMsg = when (error) {
                        is java.net.ConnectException -> "Cloud sync server not reachable (Backend offline). Local database remains safe."
                        is java.net.SocketTimeoutException -> "Cloud sync connection timed out. Local database remains safe."
                        is java.net.UnknownHostException -> "Network unavailable. Local database remains safe."
                        else -> {
                            val msg = error.message ?: ""
                            if (msg.contains("10.0.2.2") || msg.contains("localhost") || msg.contains("connect")) {
                                "Cloud sync server not reachable (Backend offline). Local database remains safe."
                            } else {
                                "Sync offline: ${msg.ifBlank { "Cloud server unreachable" }}"
                            }
                        }
                    }
                    _syncState.value = SyncState.Error(userMsg)
                }
        }
    }

    fun dismissSyncResult() {
        _syncState.value = SyncState.Idle
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository?.signOut()
            onComplete()
        }
    }
}
