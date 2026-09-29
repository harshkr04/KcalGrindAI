package com.kcalgrindai.app.core.navigation

sealed class KcalGrindRoute(
    val route: String,
    val title: String,
    val source: String,
    val flow: String,
    val nextRoutes: List<String> = emptyList(),
) {
    data object Auth : KcalGrindRoute("auth", "Sign In", "auth.html", "Auth", listOf("welcome", "home"))
    data object Welcome : KcalGrindRoute("welcome", "Welcome", "01_welcome.html", "Onboarding", listOf("goal_selection", "auth"))
    data object GoalSelection : KcalGrindRoute("goal_selection", "Goal Selection", "02_goal_selection.html", "Onboarding", listOf("onboarding_breather_goal"))
    data object BreatherGoal : KcalGrindRoute("onboarding_breather_goal", "Goal Milestones", "02b_breather_goal.html", "Onboarding", listOf("diet_preferences"))
    data object DietPreferences : KcalGrindRoute("diet_preferences", "Diet Preferences", "03_diet_preferences.html", "Onboarding", listOf("allergies"))
    data object Allergies : KcalGrindRoute("allergies", "Allergies", "04_allergies.html", "Onboarding", listOf("personal_details"))
    data object PersonalDetails : KcalGrindRoute("personal_details", "Personal Details", "05_personal_details.html", "Onboarding", listOf("activity_level"))
    data object ActivityLevel : KcalGrindRoute("activity_level", "Activity Level", "06_activity_level.html", "Onboarding", listOf("onboarding_breather_activity"))
    data object BreatherActivity : KcalGrindRoute("onboarding_breather_activity", "Activity Baseline", "06b_breather_activity.html", "Onboarding", listOf("body_metrics"))
    data object BodyMetrics : KcalGrindRoute("body_metrics", "Body Metrics", "07_body_metrics.html", "Onboarding", listOf("onboarding_breather_science"))
    data object BreatherScience : KcalGrindRoute("onboarding_breather_science", "Metabolic Science", "07b_breather_science.html", "Onboarding", listOf("calorie_target"))
    data object CalorieTarget : KcalGrindRoute("calorie_target", "Calorie Target", "08_calorie_target.html", "Onboarding", listOf("goal_setting"))
    data object GoalSetting : KcalGrindRoute("goal_setting", "Goal Setting", "09_goal_setting.html", "Onboarding", listOf("onboarding_plan_reveal"))
    data object PlanReveal : KcalGrindRoute("onboarding_plan_reveal", "Plan Ready", "09b_plan_reveal.html", "Onboarding", listOf("permission_setup"))
    data object PermissionSetup : KcalGrindRoute("permission_setup", "Permission Setup", "10_permission_setup.html", "Onboarding", listOf("home"))
    data object Home : KcalGrindRoute("home", "Home / Dashboard", "11_home.html", "Main", listOf("add_food_sheet", "diary", "insights", "profile"))
    data object Diary : KcalGrindRoute("diary", "Diary", "diary.html", "Main", listOf("meal_detail", "home", "insights"))
    data object Insights : KcalGrindRoute("insights", "Insights", "insights.html", "Main", listOf("weight_log", "home", "diary", "recipe_detail"))
    data object FoodSearch : KcalGrindRoute("food_search", "Food Search", "food_search.html", "Logging", listOf("food_detail"))
    data object FoodDetail : KcalGrindRoute("food_detail", "Food Detail", "food_detail.html", "Logging", listOf("meal_confirm"))
    data object AddFoodSheet : KcalGrindRoute("add_food_sheet", "Add Food Sheet", "add_food_sheet_redesign/code.html", "Logging", listOf("camera_scanner_redesign", "text_input", "voice_input", "barcode_scanner", "food_search", "saved_meals", "recipe_create", "custom_food"))
    data object BarcodeScanner : KcalGrindRoute("barcode_scanner", "Barcode Scanner", "barcode_scanner.html", "Logging", listOf("camera_scanner_redesign"))
    data object CameraScannerRedesign : KcalGrindRoute("camera_scanner_redesign", "Camera", "camera_scanner_redesign/code.html", "AI Logging", listOf("photo_review"))
    data object PhotoReview : KcalGrindRoute("photo_review", "Photo Review", "photo_review.html", "AI Logging", listOf("ai_analyzing", "camera_scanner_redesign"))
    data object TextInput : KcalGrindRoute("text_input", "Text Input", "text_input.html", "AI Logging", listOf("meal_confirm"))
    data object VoiceInput : KcalGrindRoute("voice_input", "Voice Input", "voice_input.html", "AI Logging", listOf("meal_confirm"))
    data object MealConfirm : KcalGrindRoute("meal_confirm", "Meal Confirm", "meal_confirm.html", "Logging", listOf("home", "diary"))
    data object MealDetail : KcalGrindRoute("meal_detail", "Meal Detail", "meal_detail.html", "Diary", listOf("food_edit"))
    data object NutritionLibrary : KcalGrindRoute("nutrition_library", "Nutrition Library", "nutrition_library.html", "Main", listOf("food_detail"))
    data object RecipeCreate : KcalGrindRoute("recipe_create", "Recipe Create", "recipe_create.html", "Logging", listOf("food_search"))
    data object SavedMeals : KcalGrindRoute("saved_meals", "Saved Meals", "saved_meals.html", "Logging", listOf("meal_confirm"))
    data object WeightLog : KcalGrindRoute("weight_log", "Weight Log", "weight_log.html", "Main", listOf("insights"))
    data object AiAnalyzing : KcalGrindRoute("ai_analyzing", "AI Analyzing", "ai_analyzing.html", "AI Logging", listOf("food_result", "food_result_multi", "food_clarify", "food_error"))
    data object FoodResult : KcalGrindRoute("food_result", "AI Result (Single Item)", "food_result.html", "AI Logging", listOf("meal_confirm"))
    data object FoodResultMulti : KcalGrindRoute("food_result_multi", "AI Result (Multiple Items)", "food_result_multi.html", "AI Logging", listOf("meal_confirm"))
    data object FoodClarify : KcalGrindRoute("food_clarify", "AI Clarify", "food_clarify.html", "AI Logging", listOf("food_result", "meal_confirm", "food_search"))
    data object AiFoodReview : KcalGrindRoute("ai_food_review", "AI Food Review", "ai_food_review.html", "AI Logging", listOf("meal_confirm", "custom_food"))
    data object FoodEditor : KcalGrindRoute("food_editor", "Food Editor", "food_editor.html", "Logging", listOf("meal_confirm"))
    data object FoodEdit : KcalGrindRoute("food_edit", "Food Edit", "food_edit.html", "Diary", listOf("meal_detail"))
    data object FoodEditing : KcalGrindRoute("food_editing", "Food Editing", "food_editing.html", "Logging", listOf("food_detail"))
    data object CustomFood : KcalGrindRoute("custom_food", "Custom Food", "custom_food.html", "Logging", listOf("meal_confirm"))
    data object FoodError : KcalGrindRoute("food_error", "Food Error", "food_error.html", "AI Logging", listOf("ai_analyzing", "food_search"))
    data object AiAssistant : KcalGrindRoute("ai_assistant", "AI Assistant", "ai_assistant.html", "Chat", listOf("ai_chat", "home", "diary", "insights", "profile"))
    data object AiChat : KcalGrindRoute("ai_chat", "AI Chat", "ai_chat.html", "Chat", listOf("meal_confirm", "ai_food_review", "ai_assistant"))
    data object AiConfirmation : KcalGrindRoute("ai_confirmation", "AI Confirmation", "ai_confirmation.html", "Chat / AI", listOf("home"))
    data object Profile : KcalGrindRoute("profile", "Profile", "user_profile/code.html", "Main", listOf("home", "auth", "subscription"))
    data object RecipeDetail : KcalGrindRoute("recipe_detail", "Recipe Detail", "recipe_detail.html", "Main", listOf("insights", "diary"))
    data object Subscription : KcalGrindRoute("subscription", "Subscription", "subscription.html", "Main", listOf("profile"))

    companion object {
        const val START_ROUTE = "welcome"

        val all: List<KcalGrindRoute>
            get() = listOf(
                Subscription,
                Auth,
                Welcome,
                GoalSelection,
                BreatherGoal,
                DietPreferences,
                Allergies,
                PersonalDetails,
                ActivityLevel,
                BreatherActivity,
                BodyMetrics,
                BreatherScience,
                CalorieTarget,
                GoalSetting,
                PlanReveal,
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
                RecipeDetail,
            )

        fun validatedRouteMap(routes: List<KcalGrindRoute> = all): Map<String, KcalGrindRoute> {
            val routeMap = routes.associateBy(KcalGrindRoute::route)
            require(routeMap.size == routes.size) {
                "KcalGrindRoute contains duplicate route keys."
            }
            require(routeMap.containsKey(START_ROUTE)) {
                "KcalGrindRoute start route '$START_ROUTE' is not registered."
            }

            val missingTargets = routes
                .flatMap { source -> source.nextRoutes.map { target -> source.route to target } }
                .filterNot { (_, target) -> routeMap.containsKey(target) }

            require(missingTargets.isEmpty()) {
                "KcalGrindRoute contains missing navigation targets: " +
                    missingTargets.joinToString { (source, target) -> "$source -> $target" }
            }

            return routeMap
        }
    }
}