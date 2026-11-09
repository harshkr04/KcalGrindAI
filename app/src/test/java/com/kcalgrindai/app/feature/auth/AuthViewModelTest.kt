package com.kcalgrindai.app.feature.auth

import com.kcalgrindai.app.fakes.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = AuthViewModel(fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasCorrectDefaults() {
        val state = viewModel.uiState.value
        assertFalse(state.isSignUpMode)
        assertFalse(state.isLoading)
        assertFalse(state.authSuccess)
        assertFalse(state.isPasswordVisible)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertNull(state.errorMessage)
    }

    @Test
    fun toggleMode_switchesBetweenSignInAndSignUp() {
        assertFalse(viewModel.uiState.value.isSignUpMode)

        viewModel.toggleMode()
        assertTrue(viewModel.uiState.value.isSignUpMode)

        viewModel.toggleMode()
        assertFalse(viewModel.uiState.value.isSignUpMode)
    }

    @Test
    fun togglePasswordVisibility_invertsState() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun submit_blankFields_showsErrors() = runTest {
        viewModel.submit()
        val state = viewModel.uiState.value
        assertEquals("Email is required", state.emailError)
        assertEquals("Password is required", state.passwordError)
        assertFalse(state.authSuccess)
    }

    @Test
    fun submit_invalidEmail_showsEmailError() = runTest {
        viewModel.onEmailChanged("not-an-email")
        viewModel.onPasswordChanged("validPassword123")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertEquals("Please enter a valid email address", state.emailError)
        assertNull(state.passwordError)
        assertFalse(state.authSuccess)
    }

    @Test
    fun submit_shortPassword_showsPasswordError() = runTest {
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("12345")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertNull(state.emailError)
        assertEquals("Password must be at least 6 characters", state.passwordError)
        assertFalse(state.authSuccess)
    }

    @Test
    fun submit_signUp_passwordsMismatch_showsError() = runTest {
        viewModel.toggleMode() // switch to Sign Up
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("differentPassword")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertEquals("Passwords do not match", state.confirmPasswordError)
        assertFalse(state.authSuccess)
    }

    @Test
    fun submit_signIn_success_updatesState() = runTest {
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.submit()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authSuccess)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun submit_signUp_success_updatesState() = runTest {
        viewModel.toggleMode()
        viewModel.onEmailChanged("newuser@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.submit()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authSuccess)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun submit_failure_mapsUserFriendlyErrorMessage() = runTest {
        fakeAuthRepository.shouldFail = true
        fakeAuthRepository.failureMessage = "The email address is already in use by another account."

        viewModel.toggleMode()
        viewModel.onEmailChanged("existing@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.submit()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.authSuccess)
        assertFalse(state.isLoading)
        assertEquals("An account with this email already exists.", state.errorMessage)
    }

    @Test
    fun continueAsGuest_success_updatesState() = runTest {
        viewModel.continueAsGuest()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authSuccess)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun continueAsGuest_failure_updatesState() = runTest {
        fakeAuthRepository.shouldFail = true
        fakeAuthRepository.failureMessage = "Anonymous auth disabled"

        viewModel.continueAsGuest()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.authSuccess)
        assertFalse(state.isLoading)
        assertEquals("Anonymous auth disabled", state.errorMessage)
    }

    @Test
    fun onGoogleSignInSuccess_success_updatesState() = runTest {
        viewModel.onGoogleSignInSuccess("sample-google-id-token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authSuccess)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun onGoogleSignInSuccess_emptyToken_setsError() = runTest {
        viewModel.onGoogleSignInSuccess("")
        val state = viewModel.uiState.value
        assertFalse(state.authSuccess)
        assertFalse(state.isLoading)
        assertEquals("Google authentication failed: Empty token.", state.errorMessage)
    }

    @Test
    fun onGoogleSignInSuccess_failure_setsError() = runTest {
        fakeAuthRepository.shouldFail = true
        fakeAuthRepository.failureMessage = "Google token invalid"

        viewModel.onGoogleSignInSuccess("invalid-token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.authSuccess)
        assertFalse(state.isLoading)
        assertEquals("Google token invalid", state.errorMessage)
    }

    @Test
    fun onGoogleSignInStarted_and_cancelled_updatesStateProperly() {
        viewModel.onGoogleSignInStarted()
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)

        viewModel.onGoogleSignInCancelled()
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.authSuccess)
    }

    @Test
    fun onGoogleSignInFailure_setsMessageAndClearsLoading() {
        viewModel.onGoogleSignInStarted()
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.onGoogleSignInFailure("Missing Web Client ID")
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Missing Web Client ID", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.authSuccess)
    }

    @Test
    fun dismissError_clearsErrorMessage() {
        viewModel.onGoogleSignInFailure("Some error")
        assertEquals("Some error", viewModel.uiState.value.errorMessage)

        viewModel.dismissError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun toggleEmailExpanded_invertsState() {
        assertFalse(viewModel.uiState.value.isEmailExpanded)
        viewModel.toggleEmailExpanded()
        assertTrue(viewModel.uiState.value.isEmailExpanded)
        viewModel.toggleEmailExpanded()
        assertFalse(viewModel.uiState.value.isEmailExpanded)
    }

    @Test
    fun submit_whileLoading_preventsDuplicateSubmission() = runTest {
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("password123")

        // First submit triggers loading
        viewModel.submit()
        assertTrue(viewModel.uiState.value.isLoading)

        // Second submit while loading should be ignored
        viewModel.submit()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.authSuccess)
    }

    @Test
    fun continueAsGuest_whileLoading_preventsDuplicateSubmission() = runTest {
        // Start an action to set loading
        viewModel.onGoogleSignInStarted()
        assertTrue(viewModel.uiState.value.isLoading)

        // continueAsGuest while loading is ignored
        viewModel.continueAsGuest()
        // Repository should not have been called because it returned early
        assertFalse(viewModel.uiState.value.authSuccess)
    }

    @Test
    fun onGoogleSignInStarted_whileLoading_preventsDuplicateSubmission() {
        viewModel.onGoogleSignInStarted()
        assertTrue(viewModel.uiState.value.isLoading)

        // Attempt second start while loading
        viewModel.onGoogleSignInStarted()
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onGoogleSignInFailure_handlesMissingWebClientIdMessage() {
        val missingConfigMessage = "Google Sign-In configuration required: OAuth Web Client ID is missing in google-services.json."
        viewModel.onGoogleSignInFailure(missingConfigMessage)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.authSuccess)
        assertEquals(missingConfigMessage, state.errorMessage)
    }

    @Test
    fun onGoogleSignInFailure_handlesReleaseFriendlyMessages() {
        val releaseFriendlyMsg = "Google Sign-In is temporarily unavailable. Please sign in with email or continue as guest."
        viewModel.onGoogleSignInFailure(releaseFriendlyMsg)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.authSuccess)
        assertEquals(releaseFriendlyMsg, state.errorMessage)
    }

    @Test
    fun onGoogleSignInFailure_networkErrorFriendlyMessage() {
        val networkMsg = "Network error during Google Sign-In. Please check your internet connection."
        viewModel.onGoogleSignInFailure(networkMsg)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.authSuccess)
        assertEquals(networkMsg, state.errorMessage)
    }

    @Test
    fun onGoogleSignInSuccess_onlySetsAuthSuccessAfterRepositorySucceeds() = runTest {
        assertFalse(viewModel.uiState.value.authSuccess)

        viewModel.onGoogleSignInSuccess("sample-valid-token")
        // Before coroutine executes to completion
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.authSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun onGoogleSignInSuccess_triggersCloudRestoreOnReinstall() = runTest {
        val fakeSyncRepo = com.kcalgrindai.app.fakes.FakeSyncRepository()
        val vmWithSync = AuthViewModel(fakeAuthRepository, fakeSyncRepo)

        vmWithSync.onGoogleSignInSuccess("sample-valid-token")
        advanceUntilIdle()

        assertTrue(vmWithSync.uiState.value.authSuccess)
        assertEquals(1, fakeSyncRepo.restoreBackupCallCount)
    }

    @Test
    fun submit_emailSignIn_triggersCloudRestore() = runTest {
        val fakeSyncRepo = com.kcalgrindai.app.fakes.FakeSyncRepository()
        val vmWithSync = AuthViewModel(fakeAuthRepository, fakeSyncRepo)

        vmWithSync.onEmailChanged("test@example.com")
        vmWithSync.onPasswordChanged("validPassword123")
        vmWithSync.submit()
        advanceUntilIdle()

        assertTrue(vmWithSync.uiState.value.authSuccess)
        assertEquals(1, fakeSyncRepo.restoreBackupCallCount)
    }

    @Test
    fun resolveEmailErrorMessage_sanitizesRawFirebaseErrorsInRelease() {
        val rawFirebaseError = RuntimeException("[ CONFIGURATION_NOT_FOUND ] Please verify Firebase configuration")

        // In release mode (isDebug = false)
        val releaseSignInMessage = AuthViewModel.resolveEmailErrorMessage(rawFirebaseError, isSignUpMode = false, isDebug = false)
        assertEquals("Unable to sign in right now. Please try again later.", releaseSignInMessage)

        val releaseSignUpMessage = AuthViewModel.resolveEmailErrorMessage(rawFirebaseError, isSignUpMode = true, isDebug = false)
        assertEquals("Unable to create account right now. Please try again later.", releaseSignUpMessage)

        // In debug mode (isDebug = true)
        val debugMessage = AuthViewModel.resolveEmailErrorMessage(rawFirebaseError, isSignUpMode = false, isDebug = true)
        assertEquals("[ CONFIGURATION_NOT_FOUND ] Please verify Firebase configuration", debugMessage)
    }

    @Test
    fun resolveEmailErrorMessage_preservesKnownActionableErrorsInRelease() {
        val credError = RuntimeException("The email address or password is incorrect")
        val credMsg = AuthViewModel.resolveEmailErrorMessage(credError, isSignUpMode = false, isDebug = false)
        assertEquals("Invalid email or password.", credMsg)

        val collisionError = RuntimeException("The email address is already in use by another account.")
        val collisionMsg = AuthViewModel.resolveEmailErrorMessage(collisionError, isSignUpMode = true, isDebug = false)
        assertEquals("An account with this email already exists.", collisionMsg)

        val networkError = RuntimeException("A network error occurred during authentication")
        val networkMsg = AuthViewModel.resolveEmailErrorMessage(networkError, isSignUpMode = false, isDebug = false)
        assertEquals("Network error. Please check your internet connection.", networkMsg)
    }

    @Test
    fun resolveGoogleAndGuestMessages_sanitizesInRelease() {
        val rawError = RuntimeException("Developer error: 10 API_NOT_CONNECTED")

        // Google
        val googleRelease = AuthViewModel.resolveGoogleErrorMessage(rawError, isDebug = false)
        assertEquals("Unable to sign in with Google right now. Please try again later.", googleRelease)
        val googleDebug = AuthViewModel.resolveGoogleErrorMessage(rawError, isDebug = true)
        assertEquals("Developer error: 10 API_NOT_CONNECTED", googleDebug)

        // Google failure string
        val clientFailRelease = AuthViewModel.resolveGoogleFailureMessage("16: Cannot find matching credential", isDebug = false)
        assertEquals("Google sign in was unsuccessful. Please try again.", clientFailRelease)

        // Guest
        val guestRelease = AuthViewModel.resolveGuestErrorMessage(rawError, isDebug = false)
        assertEquals("Unable to continue as guest right now. Please try again later.", guestRelease)
        val guestDebug = AuthViewModel.resolveGuestErrorMessage(rawError, isDebug = true)
        assertEquals("Developer error: 10 API_NOT_CONNECTED", guestDebug)
    }
}
