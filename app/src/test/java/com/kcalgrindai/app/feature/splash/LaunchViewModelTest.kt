package com.kcalgrindai.app.feature.splash

import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.TargetBasis
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.fakes.FakePhotoDraftStore
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private val noOpRecipeRepository = object : com.kcalgrindai.app.domain.repository.RecipeRepository {
        override fun observeAllRecipes() = emptyFlow<List<com.kcalgrindai.app.domain.model.Recipe>>()
        override fun observeFavoriteRecipes() = emptyFlow<List<com.kcalgrindai.app.domain.model.Recipe>>()
        override fun observeRecipeById(recipeId: String) = emptyFlow<com.kcalgrindai.app.domain.model.Recipe?>()
        override suspend fun getRecipeById(recipeId: String) = null
        override suspend fun toggleFavorite(recipeId: String) {}
        override suspend fun logRecipeToDiary(recipe: com.kcalgrindai.app.domain.model.Recipe, targetDate: java.time.LocalDate) = 1L
        override suspend fun seedRecipesIfEmpty() {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userProfileRepository = FakeUserProfileRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLaunchDestinationResolvesToWelcomeWhenNoProfile() = runTest(testDispatcher) {
        val viewModel = LaunchViewModel(
            userProfileRepository = userProfileRepository,
            recipeRepository = noOpRecipeRepository,
            foodDatabaseSeeder = null,
            photoDraftStore = FakePhotoDraftStore()
        )
        advanceUntilIdle()

        assertEquals(LaunchDestination.Welcome, viewModel.destination.value)
    }

    @Test
    fun testLaunchDestinationResolvesToHomeWhenProfileExists() = runTest(testDispatcher) {
        val profile = UserProfile(
            id = 1L,
            goal = GoalType.MAINTAIN,
            units = UnitSystem.METRIC,
            age = 25,
            heightCm = 175.0,
            weightKg = 70.0,
            goalWeightKg = 70.0,
            activityLevel = ActivityLevel.MODERATE,
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = TargetBasis.RECOMMENDED,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileRepository.saveProfile(profile)

        val viewModel = LaunchViewModel(
            userProfileRepository = userProfileRepository,
            recipeRepository = noOpRecipeRepository,
            foodDatabaseSeeder = null,
            photoDraftStore = FakePhotoDraftStore()
        )
        advanceUntilIdle()

        assertEquals(LaunchDestination.Home, viewModel.destination.value)
    }
}
