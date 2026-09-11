package com.lumina.nutrition.feature.auth

import com.lumina.nutrition.fakes.FakeAuthRepository
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
}
