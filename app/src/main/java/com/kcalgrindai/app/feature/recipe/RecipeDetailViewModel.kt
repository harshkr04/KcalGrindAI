package com.kcalgrindai.app.feature.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.Recipe
import com.kcalgrindai.app.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    val isFavorite: Boolean = false,
    val isLoggedToDiary: Boolean = false,
    val logMessage: String? = null
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navRecipeId: String = savedStateHandle.get<String>("recipeId").orEmpty()

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    init {
        if (navRecipeId.isNotEmpty()) {
            loadRecipe(navRecipeId)
        }
    }

    fun loadRecipe(id: String) {
        if (id.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            recipeRepository.observeRecipeById(id).collect { recipe ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recipe = recipe,
                        isFavorite = recipe?.isFavorite ?: false
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch {
            recipeRepository.toggleFavorite(recipe.id)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }

    fun logToDiary(onSuccess: (() -> Unit)? = null) {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch {
            recipeRepository.logRecipeToDiary(recipe, LocalDate.now())
            _uiState.update {
                it.copy(
                    isLoggedToDiary = true,
                    logMessage = "Logged '${recipe.name}' to today's diary!"
                )
            }
            onSuccess?.invoke()
        }
    }
}
