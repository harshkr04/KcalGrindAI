package com.lumina.nutrition.feature.logging.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.FoodItem
import com.lumina.nutrition.domain.repository.FoodRepository
import com.lumina.nutrition.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<FoodItem> = emptyList(),
    val cachedResults: List<FoodItem> = emptyList(),
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val loggingSessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodSearchUiState())
    val uiState: StateFlow<FoodSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
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
            _uiState.update { it.copy(cachedResults = recents) }
        }
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = true,
                            errorMessage = "You are offline. Showing cached results. Reconnect to search the full database."
                        )
                    }
                }
            )
        }
    }

    fun selectFood(food: FoodItem, onNavigateToDetail: () -> Unit) {
        loggingSessionManager.setSelectedFood(food)
        onNavigateToDetail()
    }
}
