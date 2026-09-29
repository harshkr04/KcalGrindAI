package com.kcalgrindai.app.feature.logging.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.repository.FoodRepository
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.kcalgrindai.app.domain.model.MealType

enum class FoodSearchTab {
    ALL,
    FAVORITES,
    RECENTS
}

data class FoodSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<FoodItem> = emptyList(),
    val cachedResults: List<FoodItem> = emptyList(),
    val favoriteFoods: List<FoodItem> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val selectedMealType: MealType = MealType.LUNCH,
    val selectedTab: FoodSearchTab = FoodSearchTab.ALL,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val loggingSessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FoodSearchUiState(selectedMealType = loggingSessionManager.state.value.selectedMealType)
    )
    val uiState: StateFlow<FoodSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        val currentMeal = loggingSessionManager.state.value.selectedMealType
        _uiState.update { it.copy(selectedMealType = currentMeal) }

        val prefilled = loggingSessionManager.state.value.prefilledSearchQuery
        if (!prefilled.isNullOrBlank()) {
            onQueryChanged(prefilled)
            loggingSessionManager.setPrefilledSearchQuery(null)
        } else {
            loadInitialCachedFoods()
        }
    }

    private fun loadInitialCachedFoods() {
        viewModelScope.launch {
            val recents = foodRepository.searchFoods("", 20)
            val initialFavorites = if (_uiState.value.favoriteFoods.isEmpty() && recents.isNotEmpty()) {
                recents.take(4)
            } else _uiState.value.favoriteFoods

            _uiState.update {
                it.copy(
                    cachedResults = recents,
                    favoriteFoods = initialFavorites,
                    favoriteIds = initialFavorites.map { f -> f.id }.toSet()
                )
            }
        }
    }

    fun selectMealType(mealType: MealType) {
        loggingSessionManager.setSelectedMealType(mealType)
        _uiState.update { it.copy(selectedMealType = mealType) }
    }

    fun selectTab(tab: FoodSearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFavorite(food: FoodItem) {
        val current = _uiState.value.favoriteIds
        val next = if (current.contains(food.id)) current - food.id else current + food.id
        val updatedFavorites = if (next.contains(food.id)) {
            (_uiState.value.favoriteFoods + food).distinctBy { it.id }
        } else {
            _uiState.value.favoriteFoods.filter { it.id != food.id }
        }
        _uiState.update { it.copy(favoriteIds = next, favoriteFoods = updatedFavorites) }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }

        if (newQuery.isBlank()) {
            searchJob?.cancel()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    searchResults = emptyList(),
                    isOffline = false,
                    errorMessage = null
                )
            }
            loadInitialCachedFoods()
            return
        }

        // Instant local search
        viewModelScope.launch {
            val localMatches = foodRepository.searchFoods(newQuery, 30)
            _uiState.update { it.copy(cachedResults = localMatches) }
        }

        // Debounced remote search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = foodRepository.searchFoodsOnline(newQuery)
            result.fold(
                onSuccess = { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = items,
                            isOffline = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    val isNetwork = error is java.net.UnknownHostException ||
                            error is java.net.SocketTimeoutException ||
                            error is java.net.ConnectException ||
                            error is java.io.IOException

                    val message = if (isNetwork) {
                        "You're offline. Showing foods available on this device."
                    } else {
                        "Search service temporarily unavailable."
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = isNetwork,
                            errorMessage = message
                        )
                    }
                }
            )
        }
    }

    fun selectFood(food: FoodItem, onNavigateToDetail: () -> Unit) {
        loggingSessionManager.setSelectedFood(food, _uiState.value.selectedMealType)
        onNavigateToDetail()
    }
}
