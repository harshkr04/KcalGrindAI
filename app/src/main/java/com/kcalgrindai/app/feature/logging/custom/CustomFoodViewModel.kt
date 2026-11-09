package com.kcalgrindai.app.feature.logging.custom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.FoodSource
import com.kcalgrindai.app.domain.repository.FoodRepository
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class CustomFoodUiState(
    val id: Long? = null,
    val name: String = "",
    val brand: String = "",
    val servingSize: String = "1",
    val servingUnit: String = "serving",
    val servingGrams: String = "100",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val fiber: String = "",
    val sugar: String = "",
    val sodium: String = "",
    // Validation errors
    val nameError: String? = null,
    val servingSizeError: String? = null,
    val servingUnitError: String? = null,
    val caloriesError: String? = null,
    val proteinError: String? = null,
    val carbsError: String? = null,
    val fatError: String? = null,
    val fiberError: String? = null,
    val sugarError: String? = null,
    val sodiumError: String? = null,
    val generalError: String? = null,
    // Macro warning (advisory only)
    val macroWarning: String? = null,
    val imageUri: android.net.Uri? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isEditMode: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                servingSize.toDoubleOrNull()?.let { it > 0 } == true &&
                servingUnit.isNotBlank() &&
                calories.toDoubleOrNull()?.let { it >= 0 } == true &&
                protein.toDoubleOrNull()?.let { it >= 0 } == true &&
                carbs.toDoubleOrNull()?.let { it >= 0 } == true &&
                fat.toDoubleOrNull()?.let { it >= 0 } == true
}

sealed interface CustomFoodEvent {
    data class SaveSuccess(val foodId: Long) : CustomFoodEvent
    data object DeleteSuccess : CustomFoodEvent
}

@HiltViewModel
class CustomFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    savedStateHandle: SavedStateHandle,
    private val foodImageStorageManager: com.kcalgrindai.app.core.util.FoodImageStorageManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomFoodUiState())
    val uiState: StateFlow<CustomFoodUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CustomFoodEvent>()
    val events: SharedFlow<CustomFoodEvent> = _events.asSharedFlow()

    init {
        val foodIdArg = savedStateHandle.get<Long>("foodId") ?: -1L
        if (foodIdArg > 0L) {
            loadFoodForEdit(foodIdArg)
        }
    }

    fun loadFoodForEdit(foodId: Long) {
        viewModelScope.launch {
            val food = foodRepository.getFoodById(foodId)
            if (food != null) {
                // Split servingDescription into size and unit if possible
                val parts = food.servingDescription.trim().split(" ", limit = 2)
                val size = parts.firstOrNull() ?: "1"
                val unit = if (parts.size > 1) parts[1] else "serving"

                _uiState.update {
                    it.copy(
                        id = food.id,
                        name = food.name,
                        brand = food.brand.orEmpty(),
                        servingSize = size,
                        servingUnit = unit,
                        servingGrams = if (food.servingGrams > 0) food.servingGrams.toString() else "100",
                        calories = food.calories.toInt().toString(),
                        protein = food.proteinG.toString(),
                        carbs = food.carbsG.toString(),
                        fat = food.fatG.toString(),
                        fiber = if (food.fiberG > 0) food.fiberG.toString() else "",
                        sugar = if (food.sugarG > 0) food.sugarG.toString() else "",
                        sodium = if (food.sodiumMg > 0) food.sodiumMg.toString() else "",
                        isEditMode = true
                    )
                }
                recheckMacros()
            }
        }
    }

    fun onImageSelected(uri: android.net.Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onBrandChange(value: String) {
        _uiState.update { it.copy(brand = value) }
    }

    fun onServingSizeChange(value: String) {
        _uiState.update { it.copy(servingSize = value, servingSizeError = null) }
    }

    fun onServingUnitChange(value: String) {
        _uiState.update { it.copy(servingUnit = value, servingUnitError = null) }
    }

    fun onServingGramsChange(value: String) {
        _uiState.update { it.copy(servingGrams = value) }
    }

    fun onCaloriesChange(value: String) {
        _uiState.update { it.copy(calories = value, caloriesError = null) }
        recheckMacros()
    }

    fun onProteinChange(value: String) {
        _uiState.update { it.copy(protein = value, proteinError = null) }
        recheckMacros()
    }

    fun onCarbsChange(value: String) {
        _uiState.update { it.copy(carbs = value, carbsError = null) }
        recheckMacros()
    }

    fun onFatChange(value: String) {
        _uiState.update { it.copy(fat = value, fatError = null) }
        recheckMacros()
    }

    fun onFiberChange(value: String) {
        _uiState.update { it.copy(fiber = value, fiberError = null) }
    }

    fun onSugarChange(value: String) {
        _uiState.update { it.copy(sugar = value, sugarError = null) }
    }

    fun onSodiumChange(value: String) {
        _uiState.update { it.copy(sodium = value, sodiumError = null) }
    }

    private fun recheckMacros() {
        val cals = _uiState.value.calories.toDoubleOrNull() ?: return
        val p = _uiState.value.protein.toDoubleOrNull() ?: 0.0
        val c = _uiState.value.carbs.toDoubleOrNull() ?: 0.0
        val f = _uiState.value.fat.toDoubleOrNull() ?: 0.0

        if (cals > 0 && (p > 0 || c > 0 || f > 0)) {
            val macroCheck = NutritionCalculator.checkMacros(cals.toInt(), p, c, f)
            if (!macroCheck.withinTolerance) {
                val diffStr = if (macroCheck.diffCalories > 0) "+${macroCheck.diffCalories}" else "${macroCheck.diffCalories}"
                _uiState.update {
                    it.copy(
                        macroWarning = "Macros sum to ${macroCheck.sumCalories} kcal ($diffStr kcal difference). You can still save."
                    )
                }
            } else {
                _uiState.update { it.copy(macroWarning = null) }
            }
        } else {
            _uiState.update { it.copy(macroWarning = null) }
        }
    }

    fun validate(): Boolean {
        val state = _uiState.value
        var hasError = false

        val nameError = if (state.name.isBlank()) {
            hasError = true
            "Food name is required"
        } else null

        val sizeVal = state.servingSize.toDoubleOrNull()
        val servingSizeError = when {
            state.servingSize.isBlank() -> {
                hasError = true
                "Serving size is required"
            }
            sizeVal == null || sizeVal <= 0 -> {
                hasError = true
                "Must be greater than 0"
            }
            sizeVal > 10000 -> {
                hasError = true
                "Exceeds reasonable limit"
            }
            else -> null
        }

        val servingUnitError = if (state.servingUnit.isBlank()) {
            hasError = true
            "Serving unit is required"
        } else null

        val calVal = state.calories.toDoubleOrNull()
        val caloriesError = when {
            state.calories.isBlank() -> {
                hasError = true
                "Calories are required"
            }
            calVal == null || calVal < 0 -> {
                hasError = true
                "Cannot be negative"
            }
            calVal > 5000 -> {
                hasError = true
                "Cannot exceed 5,000 kcal per serving"
            }
            else -> null
        }

        val pVal = state.protein.toDoubleOrNull()
        val proteinError = when {
            state.protein.isBlank() -> {
                hasError = true
                "Protein is required"
            }
            pVal == null || pVal < 0 -> {
                hasError = true
                "Cannot be negative"
            }
            pVal > 500 -> {
                hasError = true
                "Cannot exceed 500g"
            }
            else -> null
        }

        val cVal = state.carbs.toDoubleOrNull()
        val carbsError = when {
            state.carbs.isBlank() -> {
                hasError = true
                "Carbs are required"
            }
            cVal == null || cVal < 0 -> {
                hasError = true
                "Cannot be negative"
            }
            cVal > 500 -> {
                hasError = true
                "Cannot exceed 500g"
            }
            else -> null
        }

        val fVal = state.fat.toDoubleOrNull()
        val fatError = when {
            state.fat.isBlank() -> {
                hasError = true
                "Fat is required"
            }
            fVal == null || fVal < 0 -> {
                hasError = true
                "Cannot be negative"
            }
            fVal > 500 -> {
                hasError = true
                "Cannot exceed 500g"
            }
            else -> null
        }

        val fibVal = state.fiber.toDoubleOrNull()
        val fiberError = if (state.fiber.isNotBlank() && (fibVal == null || fibVal < 0 || fibVal > 200)) {
            hasError = true
            "Fiber must be between 0 and 200g"
        } else null

        val sugVal = state.sugar.toDoubleOrNull()
        val sugarError = if (state.sugar.isNotBlank() && (sugVal == null || sugVal < 0 || sugVal > 500)) {
            hasError = true
            "Sugar must be between 0 and 500g"
        } else null

        val sodVal = state.sodium.toDoubleOrNull()
        val sodiumError = if (state.sodium.isNotBlank() && (sodVal == null || sodVal < 0 || sodVal > 50000)) {
            hasError = true
            "Sodium must be between 0 and 50,000mg"
        } else null

        _uiState.update {
            it.copy(
                nameError = nameError,
                servingSizeError = servingSizeError,
                servingUnitError = servingUnitError,
                caloriesError = caloriesError,
                proteinError = proteinError,
                carbsError = carbsError,
                fatError = fatError,
                fiberError = fiberError,
                sugarError = sugarError,
                sodiumError = sodiumError
            )
        }

        return !hasError
    }

    fun saveCustomFood() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val servingGrams = state.servingGrams.toDoubleOrNull() ?: 100.0
                val foodItem = FoodItem(
                    id = state.id ?: 0L,
                    source = FoodSource.USER_ENTERED,
                    externalId = null,
                    name = state.name.trim(),
                    brand = state.brand.trim().ifBlank { null },
                    servingDescription = "${state.servingSize.trim()} ${state.servingUnit.trim()}".trim(),
                    servingGrams = servingGrams,
                    calories = state.calories.toDouble(),
                    proteinG = state.protein.toDouble(),
                    carbsG = state.carbs.toDouble(),
                    fatG = state.fat.toDouble(),
                    fiberG = state.fiber.toDoubleOrNull() ?: 0.0,
                    sugarG = state.sugar.toDoubleOrNull() ?: 0.0,
                    sodiumMg = state.sodium.toDoubleOrNull() ?: 0.0,
                    barcodeUpc = null,
                    isUserCreated = true,
                    createdAt = System.currentTimeMillis()
                )

                val savedId = foodRepository.saveFood(foodItem)
                val finalId = if (state.id != null && state.id > 0) state.id else savedId
                if (state.imageUri != null && finalId > 0) {
                    foodImageStorageManager?.saveCustomFoodPhoto(finalId, state.imageUri)
                }
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(CustomFoodEvent.SaveSuccess(finalId))
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, generalError = e.message ?: "Failed to save food") }
            }
        }
    }

    fun deleteCustomFood() {
        val id = _uiState.value.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                foodImageStorageManager?.deleteFoodImage(id)
                foodRepository.deleteFoodById(id)
                _uiState.update { it.copy(isDeleting = false) }
                _events.emit(CustomFoodEvent.DeleteSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeleting = false, generalError = e.message ?: "Failed to delete food") }
            }
        }
    }
}
