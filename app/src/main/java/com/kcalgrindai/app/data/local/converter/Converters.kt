package com.kcalgrindai.app.data.local.converter

import com.kcalgrindai.app.domain.model.RecipeIngredient
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromRecipeIngredientList(value: List<RecipeIngredient>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toRecipeIngredientList(value: String?): List<RecipeIngredient> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<RecipeIngredient>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
