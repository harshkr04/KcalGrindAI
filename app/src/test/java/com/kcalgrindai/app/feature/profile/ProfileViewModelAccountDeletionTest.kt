package com.kcalgrindai.app.feature.profile

import com.kcalgrindai.app.domain.model.AuthState
import com.kcalgrindai.app.domain.model.AuthUser
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import com.kcalgrindai.app.fakes.FakeUserPreferencesRepository
import com.kcalgrindai.app.domain.usecase.AccountDeletionResult
import com.kcalgrindai.app.domain.usecase.DeleteAccountUseCase
import com.kcalgrindai.app.fakes.FakeNutritionGoalRepository
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProfileViewModelAccountDeletionTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userProfileRepository = FakeUserProfileRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class LocalFakeAuthRepository(
        var reauthSuccess: Boolean = true
    ) : AuthRepository {
        var deletedFirebaseUser = false
        private val _state = MutableStateFlow<AuthState>(
            AuthState.Authenticated(AuthUser("uid_123", "test@example.com", false, "Test User"))
        )
        override fun observeAuthState(): Flow<AuthState> = _state.asStateFlow()
        override suspend fun getCurrentUser(): AuthUser? = AuthUser("uid_123", "test@example.com", false, "Test User")
        override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = Result.success(AuthUser("uid_123", "test@example.com", false, "Test"))
        override suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = Result.success(AuthUser("uid_123", email, false, "Test"))
        override suspend fun signUpWithEmail(email: String, pass: String): Result<AuthUser> = Result.success(AuthUser("uid_123", email, false, "Test"))
        override suspend fun signInAsGuest(): Result<AuthUser> = Result.success(AuthUser("guest_1", null, true, null))
        override suspend fun linkWithEmail(email: String, pass: String): Result<AuthUser> = Result.success(AuthUser("uid_123", email, false, "Test"))
        override suspend fun linkWithGoogle(idToken: String): Result<AuthUser> = Result.success(AuthUser("uid_123", "test@google.com", false, "Test"))
        override suspend fun getIdToken(forceRefresh: Boolean): String? = "fake_token"
        override suspend fun signOut() {}
        override suspend fun checkSessionFreshness(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteFirebaseUser(): Result<Unit> {
            deletedFirebaseUser = true
            return Result.success(Unit)
        }
        override suspend fun reauthenticateWithPassword(password: String): Result<Unit> {
            return if (reauthSuccess) Result.success(Unit) else Result.failure(Exception("Invalid password"))
        }
    }

    private class CustomDeleteAccountUseCase(
        private val result: AccountDeletionResult,
        auth: AuthRepository
    ) : DeleteAccountUseCase(auth, createDummyApi(), createDummyDatabase(), FakeUserPreferencesRepository()) {
        override suspend fun execute(): AccountDeletionResult = result
    }

    @Test
    fun promptDeleteAccount_setsShowConfirmDialogState() {
        val viewModel = ProfileViewModel(
            userProfileRepository = userProfileRepository,
            nutritionGoalRepository = nutritionGoalRepository
        )
        assertEquals(AccountDeletionUiState.Idle, viewModel.accountDeletionState.value)

        viewModel.promptDeleteAccount()
        assertEquals(AccountDeletionUiState.ShowConfirmDialog, viewModel.accountDeletionState.value)
    }

    @Test
    fun dismissDeleteAccount_resetsToIdle() {
        val viewModel = ProfileViewModel(
            userProfileRepository = userProfileRepository,
            nutritionGoalRepository = nutritionGoalRepository
        )
        viewModel.promptDeleteAccount()
        assertEquals(AccountDeletionUiState.ShowConfirmDialog, viewModel.accountDeletionState.value)

        viewModel.dismissDeleteAccount()
        assertEquals(AccountDeletionUiState.Idle, viewModel.accountDeletionState.value)
    }

    @Test
    fun confirmDeleteAccount_successSetsDeletedAndCallsCallback() = runTest(testDispatcher) {
        val fakeAuth = LocalFakeAuthRepository()
        val fakeUseCase = CustomDeleteAccountUseCase(AccountDeletionResult.Success, fakeAuth)
        val viewModel = ProfileViewModel(
            userProfileRepository = userProfileRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            authRepository = fakeAuth,
            deleteAccountUseCase = fakeUseCase
        )

        var navigatedToAuth = false
        viewModel.confirmDeleteAccount(onSuccess = { navigatedToAuth = true })
        advanceUntilIdle()

        assertEquals(AccountDeletionUiState.Deleted, viewModel.accountDeletionState.value)
        assertTrue(navigatedToAuth)
    }

    @Test
    fun confirmDeleteAccount_requiresRecentLoginShowsReauthDialog() = runTest(testDispatcher) {
        val fakeAuth = LocalFakeAuthRepository()
        val fakeUseCase = CustomDeleteAccountUseCase(AccountDeletionResult.RequiresRecentLogin, fakeAuth)
        val viewModel = ProfileViewModel(
            userProfileRepository = userProfileRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            authRepository = fakeAuth,
            deleteAccountUseCase = fakeUseCase
        )

        var navigated = false
        viewModel.confirmDeleteAccount(onSuccess = { navigated = true })
        advanceUntilIdle()

        assertEquals(AccountDeletionUiState.ShowReauthDialog, viewModel.accountDeletionState.value)
        assertFalse(navigated)
    }

    @Test
    fun confirmDeleteAccount_networkErrorSetsErrorState() = runTest(testDispatcher) {
        val fakeAuth = LocalFakeAuthRepository()
        val fakeUseCase = CustomDeleteAccountUseCase(
            AccountDeletionResult.NetworkError("Check internet connection"),
            fakeAuth
        )
        val viewModel = ProfileViewModel(
            userProfileRepository = userProfileRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            authRepository = fakeAuth,
            deleteAccountUseCase = fakeUseCase
        )

        viewModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertTrue(viewModel.accountDeletionState.value is AccountDeletionUiState.Error)
        assertEquals("Check internet connection", (viewModel.accountDeletionState.value as AccountDeletionUiState.Error).message)
    }

    @Test
    fun reauthenticateAndDelete_successWipesAndNavigates() = runTest(testDispatcher) {
        val fakeAuth = LocalFakeAuthRepository(reauthSuccess = true)
        val fakeUseCase = CustomDeleteAccountUseCase(AccountDeletionResult.Success, fakeAuth)
        val viewModel = ProfileViewModel(
            userProfileRepository = userProfileRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            authRepository = fakeAuth,
            deleteAccountUseCase = fakeUseCase
        )

        var navigated = false
        viewModel.reauthenticateAndDelete("correct_password", onSuccess = { navigated = true })
        advanceUntilIdle()

        assertEquals(AccountDeletionUiState.Deleted, viewModel.accountDeletionState.value)
        assertTrue(navigated)
    }

    @Test
    fun reauthenticateAndDelete_wrongPasswordSetsError() = runTest(testDispatcher) {
        val fakeAuth = LocalFakeAuthRepository(reauthSuccess = false)
        val fakeUseCase = CustomDeleteAccountUseCase(AccountDeletionResult.Success, fakeAuth)
        val viewModel = ProfileViewModel(
            userProfileRepository = userProfileRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            authRepository = fakeAuth,
            deleteAccountUseCase = fakeUseCase
        )

        var navigated = false
        viewModel.reauthenticateAndDelete("wrong_password", onSuccess = { navigated = true })
        advanceUntilIdle()

        assertTrue(viewModel.accountDeletionState.value is AccountDeletionUiState.Error)
        assertFalse(navigated)
    }

    companion object {
        fun createDummyApi() = object : com.kcalgrindai.app.data.remote.api.AccountApiService {
            override suspend fun deleteAccount(): com.kcalgrindai.app.data.remote.api.AccountDeletionResponse {
                return com.kcalgrindai.app.data.remote.api.AccountDeletionResponse(true, "OK")
            }
        }



        fun createDummyDatabase(): com.kcalgrindai.app.data.local.KcalGrindDatabase {
            val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            return androidx.room.Room.inMemoryDatabaseBuilder(
                context,
                com.kcalgrindai.app.data.local.KcalGrindDatabase::class.java
            ).allowMainThreadQueries().build()
        }
    }
}
