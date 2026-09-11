package com.lumina.nutrition.core.common

sealed interface AppResult<out T> {
    data object Loading : AppResult<Nothing>
    data class Success<T>(val value: T) : AppResult<T>
    data class Error(val throwable: Throwable? = null, val message: String? = null) : AppResult<Nothing>
}
