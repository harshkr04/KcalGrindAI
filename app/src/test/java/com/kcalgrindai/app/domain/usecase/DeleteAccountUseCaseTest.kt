package com.kcalgrindai.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.core.util.NetworkMonitor
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.FoodEntity
import com.kcalgrindai.app.data.remote.api.AccountApiService
import com.kcalgrindai.app.data.remote.api.AccountDeletionResponse
import com.kcalgrindai.app.domain.model.AuthState
import com.kcalgrindai.app.domain.model.AuthUser
import com.kcalgrindai.app.domain.model.ThemeMode
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import com.kcalgrindai.app.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class DeleteAccountUseCaseTest {

    private lateinit var database: KcalGrindDatabase
    private lateinit var fakePrefs: FakeUserPreferencesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fakePrefs = FakeUserPreferencesRepository()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createFoodEntity(id: Long, name: String) = FoodEntity(
        id = id,
        source = "USER_ENTERED",
        externalId = null,
        name = name,
        brand = null,
        servingDescription = "1 serving",
        servingGrams = 100.0,
        calories = 100.0,
        proteinG = 10.0,
        carbsG = 10.0,
        fatG = 2.0,
        fiberG = 1.0,
        sugarG = 1.0,
        sodiumMg = 50.0,
        barcodeUpc = null,
        isUserCreated = true,
        createdAt = System.currentTimeMillis()
    )



    private class TestAuthRepo(
        private var user: AuthUser? = AuthUser("uid_1", "user@test.com", false, "Test"),
        var deleteResult: Result<Unit> = Result.success(Unit),
        var freshnessResult: Result<Unit> = Result.success(Unit)
    ) : AuthRepository {
        override suspend fun checkSessionFreshness(): Result<Unit> = freshnessResult
        var userDeleted = false
        private val _state = MutableStateFlow<AuthState>(
            if (user != null) AuthState.Authenticated(user!!) else AuthState.Unauthenticated
        )
        override fun observeAuthState(): Flow<AuthState> = _state.asStateFlow()
        override suspend fun getCurrentUser(): AuthUser? = user
        override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = Result.success(user!!)
        override suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = Result.success(user!!)
        override suspend fun signUpWithEmail(email: String, pass: String): Result<AuthUser> = Result.success(user!!)
        override suspend fun signInAsGuest(): Result<AuthUser> = Result.success(AuthUser("guest_1", null, true, null))
        override suspend fun linkWithEmail(email: String, pass: String): Result<AuthUser> = Result.success(user!!)
        override suspend fun linkWithGoogle(idToken: String): Result<AuthUser> = Result.success(user!!)
        override suspend fun getIdToken(forceRefresh: Boolean): String? = "token"
        override suspend fun signOut() { user = null }
        override suspend fun deleteFirebaseUser(): Result<Unit> {
            if (deleteResult.isSuccess) {
                userDeleted = true
                user = null
            }
            return deleteResult
        }
        override suspend fun reauthenticateWithPassword(password: String): Result<Unit> = Result.success(Unit)
    }

    private class TestApiService(
        var responseToReturn: AccountDeletionResponse = AccountDeletionResponse(true, "uid_1", "2026-11-04"),
        var exceptionToThrow: Exception? = null
    ) : AccountApiService {
        var callCount = 0
        override suspend fun deleteAccount(): AccountDeletionResponse {
            callCount++
            exceptionToThrow?.let { throw it }
            return responseToReturn
        }
    }

    private class TestNetworkMonitor(private var online: Boolean = true) : NetworkMonitor {
        override val isOnline: Flow<Boolean> = emptyFlow()
        override fun isCurrentlyOnline(): Boolean = online
        fun setOnline(value: Boolean) { online = value }
    }

    @Test
    fun guestUser_wipesLocalDatabaseAndPreferencesWithoutNetworkCall() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(1L, "Apple"))
        val authRepo = TestAuthRepo(user = null)
        val apiService = TestApiService()

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs
        )

        val result = useCase.execute()

        assertEquals(AccountDeletionResult.Success, result)
        assertEquals(0, apiService.callCount) // No network called for guest
        assertTrue(fakePrefs.wasCleared) // Preferences wiped
        assertEquals(0, database.foodDao().searchFoods("Apple").size) // DB tables cleared
    }

    @Test
    fun authenticatedUser_callsBackendDeletesAuthAndWipesLocalState() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(2L, "Banana"))
        val authRepo = TestAuthRepo()
        val apiService = TestApiService()

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs
        )

        val result = useCase.execute()

        assertEquals(AccountDeletionResult.Success, result)
        assertEquals(1, apiService.callCount)
        assertTrue(authRepo.userDeleted)
        assertTrue(fakePrefs.wasCleared)
        assertEquals(0, database.foodDao().searchFoods("Banana").size)
    }

    @Test
    fun offline_abortsWithoutWipingLocalState() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(3L, "Orange"))
        val authRepo = TestAuthRepo()
        val apiService = TestApiService()
        val networkMonitor = TestNetworkMonitor(online = false)

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs,
            networkMonitor = networkMonitor
        )

        val result = useCase.execute()

        assertTrue(result is AccountDeletionResult.NetworkError)
        assertEquals(0, apiService.callCount)
        assertFalse(authRepo.userDeleted)
        assertFalse(fakePrefs.wasCleared) // Crucial: No data loss
        assertEquals(1, database.foodDao().searchFoods("Orange").size) // Local DB intact
    }

    @Test
    fun backendServerError_abortsWithoutWipingLocalState() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(4L, "Kiwi"))
        val authRepo = TestAuthRepo()
        val errorResponse = Response.error<AccountDeletionResponse>(
            500,
            "Internal Server Error".toResponseBody("text/plain".toMediaType())
        )
        val apiService = TestApiService(exceptionToThrow = HttpException(errorResponse))

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs
        )

        val result = useCase.execute()

        assertTrue(result is AccountDeletionResult.BackendError)
        assertFalse(authRepo.userDeleted)
        assertFalse(fakePrefs.wasCleared)
        assertEquals(1, database.foodDao().searchFoods("Kiwi").size)
    }

    @Test
    fun staleSessionBeforeBackendPurge_stopsEarlyWithRequiresRecentLoginAndDoesNotCallBackend() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(5L, "Grape"))
        val authRepo = TestAuthRepo(
            freshnessResult = Result.failure(Exception("CREDENTIAL_TOO_OLD_LOGIN_AGAIN [recent-login]"))
        )
        val apiService = TestApiService()

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs
        )

        val result = useCase.execute()

        assertEquals(AccountDeletionResult.RequiresRecentLogin, result)
        assertEquals(0, apiService.callCount) // Destructive backend purge was NEVER called
        assertFalse(authRepo.userDeleted)
        assertFalse(fakePrefs.wasCleared)
        assertEquals(1, database.foodDao().searchFoods("Grape").size)

        // Re-authenticate and retry -> Now succeeds with 1 purge call
        authRepo.freshnessResult = Result.success(Unit)
        val retryResult = useCase.execute()
        assertEquals(AccountDeletionResult.Success, retryResult)
        assertEquals(1, apiService.callCount)
        assertTrue(authRepo.userDeleted)
        assertTrue(fakePrefs.wasCleared)
        assertEquals(0, database.foodDao().searchFoods("Grape").size)
    }

    @Test
    fun reauthRequiredAfterBackendPurge_retryIsIdempotentAndSecondPurgeIsNoOp() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(6L, "Mango"))
        val authRepo = TestAuthRepo(
            freshnessResult = Result.success(Unit),
            deleteResult = Result.failure(Exception("CREDENTIAL_TOO_OLD_LOGIN_AGAIN [recent-login]"))
        )
        val apiService = TestApiService()

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs
        )

        // First attempt: Freshness passed, backend purged, but delete threw RequiresRecentLogin
        val firstResult = useCase.execute()
        assertEquals(AccountDeletionResult.RequiresRecentLogin, firstResult)
        assertEquals(1, apiService.callCount)
        assertFalse(authRepo.userDeleted)
        assertFalse(fakePrefs.wasCleared)
        assertEquals(1, database.foodDao().searchFoods("Mango").size)

        // User re-authenticates:
        val reauthRes = authRepo.reauthenticateWithPassword("password123")
        assertTrue(reauthRes.isSuccess)
        authRepo.deleteResult = Result.success(Unit)

        // Second attempt: Second purge call is an idempotent no-op (200 OK)
        val secondResult = useCase.execute()
        assertEquals(AccountDeletionResult.Success, secondResult)
        assertEquals(2, apiService.callCount)
        assertTrue(authRepo.userDeleted)
        assertTrue(fakePrefs.wasCleared)
        assertEquals(0, database.foodDao().searchFoods("Mango").size)
    }

    @Test
    fun http401Unauthorized_returnsRequiresRecentLoginWithoutWipingLocalState() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(7L, "Peach"))
        val authRepo = TestAuthRepo()
        val errorResponse = Response.error<AccountDeletionResponse>(
            401,
            "Unauthorized".toResponseBody("text/plain".toMediaType())
        )
        val apiService = TestApiService(exceptionToThrow = HttpException(errorResponse))

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs
        )

        val result = useCase.execute()

        assertEquals(AccountDeletionResult.RequiresRecentLogin, result)
        assertFalse(authRepo.userDeleted)
        assertFalse(fakePrefs.wasCleared)
        assertEquals(1, database.foodDao().searchFoods("Peach").size)
    }

    @Test
    fun http403Forbidden_returnsExplicitFailureMessageWithoutWipingLocalState() = runBlocking {
        database.foodDao().insertFood(createFoodEntity(8L, "Plum"))
        val authRepo = TestAuthRepo()
        val errorResponse = Response.error<AccountDeletionResponse>(
            403,
            "Forbidden".toResponseBody("text/plain".toMediaType())
        )
        val apiService = TestApiService(exceptionToThrow = HttpException(errorResponse))

        val useCase = DeleteAccountUseCase(
            authRepository = authRepo,
            accountApiService = apiService,
            database = database,
            userPreferencesRepository = fakePrefs
        )

        val result = useCase.execute()

        assertTrue(result is AccountDeletionResult.Failure)
        assertTrue((result as AccountDeletionResult.Failure).message.contains("forbidden", ignoreCase = true))
        assertFalse(authRepo.userDeleted)
        assertFalse(fakePrefs.wasCleared)
        assertEquals(1, database.foodDao().searchFoods("Plum").size)
    }
}
