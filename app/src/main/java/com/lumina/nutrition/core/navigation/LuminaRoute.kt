package com.lumina.nutrition.core.navigation

sealed class LuminaRoute(
    val route: String,
    val title: String,
    val source: String,
    val flow: String,
    val nextRoutes: List<String> = emptyList(),
) {
    data object Auth : LuminaRoute("auth", "Sign In", "auth.html", "Auth", listOf("welcome", "home"))
    data object Welcome : LuminaRoute("welcome", "Welcome", "01_welcome.html", "Onboarding", listOf("goal_selection", "auth"))
    data object GoalSelection : LuminaRoute("goal_selection", "Goal Selection", "02_goal_selection.html", "Onboarding", listOf("diet_preferences"))
    data object DietPreferences : LuminaRoute("diet_preferences", "Diet Preferences", "03_diet_preferences.html", "Onboarding", listOf("allergies"))
    data object Allergies : LuminaRoute("allergies", "Allergies", "04_allergies.html", "Onboarding", listOf("personal_details"))
    data object PersonalDetails : LuminaRoute("personal_details", "Personal Details", "05_personal_details.html", "Onboarding", listOf("activity_level"))
    data object ActivityLevel : LuminaRoute("activity_level", "Activity Level", "06_activity_level.html", "Onboarding", listOf("body_metrics"))
    data object BodyMetrics : LuminaRoute("body_metrics", "Body Metrics", "07_body_metrics.html", "Onboarding", listOf("calorie_target"))
    data object CalorieTarget : LuminaRoute("calorie_target", "Calorie Target", "08_calorie_target.html", "Onboarding", listOf("goal_setting"))
    data object GoalSetting : LuminaRoute("goal_setting", "Goal Setting", "09_goal_setting.html", "Onboarding", listOf("permission_setup"))
    data object PermissionSetup : LuminaRoute("permission_setup", "Permission Setup", "10_permission_setup.html", "Onboarding", listOf("home"))
    data object Home : LuminaRoute("home", "Home / Dashboard", "11_home.html", "Main", listOf("add_food_sheet", "diary", "insights", "profile"))
    data object Diary : LuminaRoute("diary", "Diary", "diary.html", "Main", listOf("meal_detail", "home", "insights"))
    data object Insights : LuminaRoute("insights", "Insights", "insights.html", "Main", listOf("weight_log", "home", "diary"))
    data object FoodSearch : LuminaRoute("food_search", "Food Search", "food_search.html", "Logging", listOf("food_detail"))
    data object FoodDetail : LuminaRoute("food_detail", "Food Detail", "food_detail.html", "Logging", listOf("meal_confirm"))
    data object AddFoodSheet : LuminaRoute("add_food_sheet", "Add Food Sheet", "add_food_sheet_redesign/code.html", "Logging", listOf("camera_scanner_redesign", "text_input", "voice_input", "barcode_scanner", "food_search", "saved_meals", "recipe_create", "custom_food"))
    data object BarcodeScanner : LuminaRoute("barcode_scanner", "Barcode Scanner", "barcode_scanner.html", "Logging", listOf("camera_scanner_redesign"))
    data object CameraScannerRedesign : LuminaRoute("camera_scanner_redesign", "Camera", "camera_scanner_redesign/code.html", "AI Logging", listOf("photo_review"))
    data object PhotoReview : LuminaRoute("photo_review", "Photo Review", "photo_review.html", "AI Logging", listOf("ai_analyzing", "camera_scanner_redesign"))
    data object TextInput : LuminaRoute("text_input", "Text Input", "text_input.html", "AI Logging", listOf("meal_confirm"))
    data object VoiceInput : LuminaRoute("voice_input", "Voice Input", "voice_input.html", "AI Logging", listOf("meal_confirm"))
    data object MealConfirm : LuminaRoute("meal_confirm", "Meal Confirm", "meal_confirm.html", "Logging", listOf("home", "diary"))
    data object MealDetail : LuminaRoute("meal_detail", "Meal Detail", "meal_detail.html", "Diary", listOf("food_edit"))
    data object NutritionLibrary : LuminaRoute("nutrition_library", "Nutrition Library", "nutrition_library.html", "Main", listOf("food_detail"))
    data object RecipeCreate : LuminaRoute("recipe_create", "Recipe Create", "recipe_create.html", "Logging", listOf("food_search"))
    data object SavedMeals : LuminaRoute("saved_meals", "Saved Meals", "saved_meals.html", "Logging", listOf("meal_confirm"))
    data object WeightLog : LuminaRoute("weight_log", "Weight Log", "weight_log.html", "Main", listOf("insights"))
    data object AiAnalyzing : LuminaRoute("ai_analyzing", "AI Analyzing", "ai_analyzing.html", "AI Logging", listOf("food_result", "food_result_multi", "food_clarify", "food_error"))
    data object FoodResult : LuminaRoute("food_result", "AI Result (Single Item)", "food_result.html", "AI Logging", listOf("meal_confirm"))
    data object FoodResultMulti : LuminaRoute("food_result_multi", "AI Result (Multiple Items)", "food_result_multi.html", "AI Logging", listOf("meal_confirm"))
    data object FoodClarify : LuminaRoute("food_clarify", "AI Clarify", "food_clarify.html", "AI Logging", listOf("food_result", "meal_confirm", "food_search"))
    data object AiFoodReview : LuminaRoute("ai_food_review", "AI Food Review", "ai_food_review.html", "AI Logging", listOf("meal_confirm", "custom_food"))
    data object FoodEditor : LuminaRoute("food_editor", "Food Editor", "food_editor.html", "Logging", listOf("meal_confirm"))
    data object FoodEdit : LuminaRoute("food_edit", "Food Edit", "food_edit.html", "Diary", listOf("meal_detail"))
    data object FoodEditing : LuminaRoute("food_editing", "Food Editing", "food_editing.html", "Logging", listOf("food_detail"))
    data object CustomFood : LuminaRoute("custom_food", "Custom Food", "custom_food.html", "Logging", listOf("meal_confirm"))
    data object FoodError : LuminaRoute("food_error", "Food Error", "food_error.html", "AI Logging", listOf("ai_analyzing", "food_search"))
    data object AiAssistant : LuminaRoute("ai_assistant", "AI Assistant", "ai_assistant.html", "Chat", listOf("ai_chat", "home", "diary", "insights", "profile"))
    data object AiChat : LuminaRoute("ai_chat", "AI Chat", "ai_chat.html", "Chat", listOf("meal_confirm", "ai_food_review", "ai_assistant"))
    data object AiConfirmation : LuminaRoute("ai_confirmation", "AI Confirmation", "ai_confirmation.html", "Chat / AI", listOf("home"))
    data object Profile : LuminaRoute("profile", "Profile", "user_profile/code.html", "Main", listOf("home", "auth"))

    companion object {
        const val START_ROUTE = "welcome"

        val all: List<LuminaRoute>
            get() = listOf(
                Auth,
                Welcome,
                GoalSelection,
                DietPreferences,
                Allergies,
                PersonalDetails,
                ActivityLevel,
                BodyMetrics,
                CalorieTarget,
                GoalSetting,
                PermissionSetup,
                Home,
                Diary,
                Insights,
                FoodSearch,
                FoodDetail,
                AddFoodSheet,
                BarcodeScanner,
                CameraScannerRedesign,
                PhotoReview,
                TextInput,
                VoiceInput,
                MealConfirm,
                MealDetail,
                NutritionLibrary,
                RecipeCreate,
                SavedMeals,
                WeightLog,
                AiAnalyzing,
                FoodResult,
                FoodResultMulti,
                FoodClarify,
                AiFoodReview,
                FoodEditor,
                FoodEdit,
                FoodEditing,
                CustomFood,
                FoodError,
                AiAssistant,
                AiChat,
                AiConfirmation,
                Profile,
            )

        fun validatedRouteMap(routes: List<LuminaRoute> = all): Map<String, LuminaRoute> {
            val routeMap = routes.associateBy(LuminaRoute::route)
            require(routeMap.size == routes.size) {
                "LuminaRoute contains duplicate route keys."
            }
            require(routeMap.containsKey(START_ROUTE)) {
                "LuminaRoute start route '$START_ROUTE' is not registered."
            }

            val missingTargets = routes
                .flatMap { source -> source.nextRoutes.map { target -> source.route to target } }
                .filterNot { (_, target) -> routeMap.containsKey(target) }

            require(missingTargets.isEmpty()) {
                "LuminaRoute contains missing navigation targets: " +
                    missingTargets.joinToString { (source, target) -> "$source -> $target" }
            }

            return routeMap
        }
    }
}