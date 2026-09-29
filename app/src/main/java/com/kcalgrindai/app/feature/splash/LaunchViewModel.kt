package com.kcalgrindai.app.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.core.util.PhotoDraftStore
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LaunchDestination {
    data object Loading : LaunchDestination
    data object Home : LaunchDestination
    data object Welcome : LaunchDestination
}

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val recipeRepository: com.kcalgrindai.app.domain.repository.RecipeRepository,
    private val foodDatabaseSeeder: com.kcalgrindai.app.data.local.seed.FoodDatabaseSeeder? = null,
    private val photoDraftStore: PhotoDraftStore
) : ViewModel() {

    private val _destination = MutableStateFlow<LaunchDestination>(LaunchDestination.Loading)
    val destination: StateFlow<LaunchDestination> = _destination.asStateFlow()

    init {
        checkUserProfile()
        seedDatabase()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            photoDraftStore.purgeStaleTempFiles()
        }
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            foodDatabaseSeeder?.seedIfNeeded()
            recipeRepository.seedRecipesIfEmpty()
        }
    }

    fun checkUserProfile() {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            if (profile != null) {
                _destination.value = LaunchDestination.Home
            } else {
                _destination.value = LaunchDestination.Welcome
            }
        }
    }
}
