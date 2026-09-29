package com.kcalgrindai.app.domain.model

enum class GoalType(val id: String, val label: String, val blurb: String, val delta: Int, val proteinPerKg: Double) {
    LOSE("lose", "Lose weight", "Steady, sustainable fat loss.", -450, 1.8),
    MAINTAIN("maintain", "Maintain weight", "Hold steady and eat consistently.", 0, 1.6),
    GAIN("gain", "Gain weight", "Add weight at a comfortable pace.", 350, 1.7),
    MUSCLE("muscle", "Build muscle", "Fuel training and lean gains.", 250, 2.0),
    HEALTHIER("healthier", "Eat healthier", "Better quality, no calorie chase.", 0, 1.5),
    NUTRITION("nutrition", "Track nutrition", "Just see what you eat, clearly.", 0, 1.6);

    companion object {
        fun fromId(id: String): GoalType {
            val normalized = id.trim().lowercase()
            val matched = entries.find { it.id == normalized || it.name.equals(id, ignoreCase = true) }
                ?: when (normalized) {
                    "lose_weight" -> LOSE
                    "gain_weight" -> GAIN
                    "build_muscle" -> MUSCLE
                    "eat_healthier" -> HEALTHIER
                    "track_nutrition" -> NUTRITION
                    else -> null
                }
            if (matched == null) {
                System.err.println("WARN [GoalType]: Unknown goal id '$id', falling back to MAINTAIN")
            }
            return matched ?: MAINTAIN
        }
    }
}

enum class UnitSystem(val id: String) {
    METRIC("metric"),
    IMPERIAL("imperial");

    companion object {
        fun fromId(id: String): UnitSystem = entries.find { it.id.equals(id, ignoreCase = true) } ?: METRIC
    }
}

enum class ActivityLevel(val id: String, val label: String, val blurb: String, val factor: Double) {
    SEDENTARY("sedentary", "Sedentary", "Little exercise", 1.20),
    LIGHT("light", "Lightly active", "Light exercise 1–3 days/week", 1.375),
    MODERATE("moderate", "Moderately active", "Exercise 3–5 days/week", 1.55),
    VERY("very", "Very active", "Hard exercise 6–7 days/week", 1.725),
    EXTREME("extreme", "Extremely active", "Very intense physical activity", 1.90);

    companion object {
        fun fromId(id: String): ActivityLevel = entries.find { it.id.equals(id, ignoreCase = true) } ?: MODERATE
    }
}

enum class MealType(val id: String, val label: String) {
    BREAKFAST("breakfast", "Breakfast"),
    LUNCH("lunch", "Lunch"),
    DINNER("dinner", "Dinner"),
    SNACK("snack", "Snack");

    companion object {
        fun fromId(id: String): MealType = entries.find { it.id.equals(id, ignoreCase = true) } ?: SNACK
    }
}

enum class LogSource(val id: String) {
    MANUAL("manual"),
    AI_PHOTO("ai_photo"),
    AI_VOICE("ai_voice"),
    AI_TEXT("ai_text"),
    BARCODE("barcode"),
    SEARCH("search"),
    RECIPE("recipe"),
    CURATED_RECIPE("curated_recipe");

    companion object {
        fun fromId(id: String): LogSource = entries.find { it.id.equals(id, ignoreCase = true) } ?: MANUAL
    }
}

enum class ItemSource(val id: String) {
    MANUAL("manual"),
    AI("ai"),
    VERIFIED("verified"),
    RECIPE("recipe"),
    CURATED_RECIPE("curated_recipe");

    companion object {
        fun fromId(id: String): ItemSource = entries.find { it.id.equals(id, ignoreCase = true) } ?: MANUAL
    }
}

enum class FoodSource(val id: String) {
    USDA("usda"),
    OPEN_FOOD_FACTS("openfoodfacts"),
    NUTRITIONIX("nutritionix"),
    CUSTOM("custom"),
    IFCT("ifct");

    companion object {
        fun fromId(id: String): FoodSource = entries.find { it.id.equals(id, ignoreCase = true) } ?: CUSTOM
    }
}

enum class TargetBasis(val id: String) {
    RECOMMENDED("recommended"),
    CUSTOM("custom"),
    FALLBACK("fallback");

    companion object {
        fun fromId(id: String): TargetBasis = entries.find { it.id.equals(id, ignoreCase = true) } ?: RECOMMENDED
    }
}

enum class MessageRole(val id: String) {
    USER("user"),
    ASSISTANT("assistant");

    companion object {
        fun fromId(id: String): MessageRole = entries.find { it.id.equals(id, ignoreCase = true) } ?: USER
    }
}
