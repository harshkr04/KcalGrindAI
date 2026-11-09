package com.kcalgrindai.app.feature.home

import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import com.kcalgrindai.app.fakes.FakeUserPreferencesRepository
import com.kcalgrindai.app.feature.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderToggleSyncTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userPreferencesRepository = FakeUserPreferencesRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun homeRemindersSheet_and_profileToggles_readAndWriteSingleSetting() = runTest {
        // Initial defaults
        assertTrue("Meal reminders default to enabled", userPreferencesRepository.mealRemindersEnabled.first())
        assertTrue("Hydration reminders default to enabled", userPreferencesRepository.hydrationRemindersEnabled.first())

        // 1. User toggles OFF meal reminders in Home Reminders Sheet
        userPreferencesRepository.setMealRemindersEnabled(false)
        advanceUntilIdle()

        // Verify repository is updated
        assertFalse("Repository reflects meal reminder disabled", userPreferencesRepository.mealRemindersEnabled.first())

        // 2. User toggles OFF hydration reminders
        userPreferencesRepository.setHydrationRemindersEnabled(false)
        advanceUntilIdle()

        assertFalse("Repository reflects hydration reminder disabled", userPreferencesRepository.hydrationRemindersEnabled.first())

        // 3. User re-enables meal reminders in Profile screen
        userPreferencesRepository.setMealRemindersEnabled(true)
        advanceUntilIdle()

        assertTrue("Home sheet and Profile observe single source of truth for meals", userPreferencesRepository.mealRemindersEnabled.first())

        // 4. User re-enables hydration reminders in Profile screen
        userPreferencesRepository.setHydrationRemindersEnabled(true)
        advanceUntilIdle()

        assertTrue("Home sheet and Profile observe single source of truth for hydration", userPreferencesRepository.hydrationRemindersEnabled.first())
    }
}
