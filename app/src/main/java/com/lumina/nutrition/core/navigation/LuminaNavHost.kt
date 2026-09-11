package com.lumina.nutrition.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.feature.aicoach.AiChatScreen
import com.lumina.nutrition.feature.diary.DiaryScreen
import com.lumina.nutrition.feature.diary.detail.MealDetailScreen
import com.lumina.nutrition.feature.home.HomeScreen
import com.lumina.nutrition.feature.insights.InsightsSkeletonScreen
import com.lumina.nutrition.feature.logging.LoggingSkeletonScreen
import com.lumina.nutrition.feature.logging.clarify.FoodClarifyScreen
import com.lumina.nutrition.feature.logging.confirm.MealConfirmScreen
import com.lumina.nutrition.feature.logging.photo.PhotoReviewScreen
import com.lumina.nutrition.feature.logging.result.FoodResultMultiScreen
import com.lumina.nutrition.feature.logging.result.FoodResultScreen
import com.lumina.nutrition.feature.logging.review.AiFoodReviewScreen
import com.lumina.nutrition.feature.logging.state.LoggingSessionManager
import com.lumina.nutrition.feature.logging.text.TextInputScreen
import com.lumina.nutrition.feature.logging.voice.VoiceInputScreen
import com.lumina.nutrition.feature.onboarding.activity.ActivityLevelScreen
import com.lumina.nutrition.feature.onboarding.allergies.AllergiesScreen
import com.lumina.nutrition.feature.onboarding.body.BodyMetricsScreen
import com.lumina.nutrition.feature.onboarding.diet.DietPreferencesScreen
import com.lumina.nutrition.feature.onboarding.goal.GoalSelectionScreen
import com.lumina.nutrition.feature.onboarding.goalsetting.GoalSettingScreen
import com.lumina.nutrition.feature.onboarding.permissions.PermissionSetupScreen
import com.lumina.nutrition.feature.onboarding.personal.PersonalDetailsScreen
import com.lumina.nutrition.feature.onboarding.target.CalorieTargetScreen
import com.lumina.nutrition.feature.onboarding.welcome.WelcomeScreen
import com.lumina.nutrition.feature.profile.ProfileScreen
import com.lumina.nutrition.feature.splash.LaunchDestination
import com.lumina.nutrition.feature.splash.LaunchViewModel

@Composable
fun LuminaNavHost(
    navController: NavHostController = rememberNavController(),
    launchViewModel: LaunchViewModel = hiltViewModel(),
    loggingNavViewModel: com.lumina.nutrition.feature.logging.state.LoggingNavigationViewModel = hiltViewModel()
) {
    val sessionManager = loggingNavViewModel.sessionManager
    val destinations = remember { LuminaRoute.all }
    val routeMap = remember(destinations) { LuminaRoute.validatedRouteMap(destinations) }
    val launchDestination by launchViewModel.destination.collectAsState()

    val navigateToRoute: (String) -> Unit = { route ->
        navController.navigate(routeMap.getValue(route).route)
    }

    val onNavigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(LuminaRoute.Home.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (launchDestination is LaunchDestination.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LuminaBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = LuminaPrimary)
        }
        return
    }

    val startRoute = if (launchDestination is LaunchDestination.Home) {
        LuminaRoute.Home.route
    } else {
        LuminaRoute.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startRoute,
    ) {
        destinations.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    LuminaRoute.Welcome -> {
                        WelcomeScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.GoalSelection.route) },
                            onNavigateToAuth = { navController.navigate(LuminaRoute.Auth.route) }
                        )
                    }
                    LuminaRoute.Auth -> {
                        com.lumina.nutrition.feature.auth.AuthScreen(
                            onAuthSuccess = {
                                launchViewModel.checkUserProfile()
                                navController.navigate(LuminaRoute.Home.route) {
                                    popUpTo(LuminaRoute.Welcome.route) { inclusive = true }
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.GoalSelection -> {
                        GoalSelectionScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.DietPreferences.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.DietPreferences -> {
                        DietPreferencesScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.Allergies.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.Allergies -> {
                        AllergiesScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.PersonalDetails.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.PersonalDetails -> {
                        PersonalDetailsScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.ActivityLevel.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.ActivityLevel -> {
                        ActivityLevelScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.BodyMetrics.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.BodyMetrics -> {
                        BodyMetricsScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.CalorieTarget.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.CalorieTarget -> {
                        CalorieTargetScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.GoalSetting.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.GoalSetting -> {
                        GoalSettingScreen(
                            onNavigateNext = { navController.navigate(LuminaRoute.PermissionSetup.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.PermissionSetup -> {
                        PermissionSetupScreen(
                            onNavigateHome = {
                                navController.navigate(LuminaRoute.Home.route) {
                                    popUpTo(LuminaRoute.Welcome.route) { inclusive = true }
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.Profile -> {
                        ProfileScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(LuminaRoute.AddFoodSheet.route) },
                            onNavigateToAuth = { navController.navigate(LuminaRoute.Auth.route) }
                        )
                    }
                    LuminaRoute.AddFoodSheet -> {
                        com.lumina.nutrition.feature.logging.sheet.AddFoodSheetScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSearch = { navController.navigate(LuminaRoute.FoodSearch.route) },
                            onNavigateToBarcode = { navController.navigate(LuminaRoute.CameraScannerRedesign.route) },
                            onNavigateToPhoto = { navController.navigate(LuminaRoute.PhotoReview.route) },
                            onNavigateToVoice = { navController.navigate(LuminaRoute.VoiceInput.route) },
                            onNavigateToText = { navController.navigate(LuminaRoute.TextInput.route) }
                        )
                    }
                    LuminaRoute.FoodSearch -> {
                        com.lumina.nutrition.feature.logging.search.FoodSearchScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { navController.navigate(LuminaRoute.FoodDetail.route) },
                            onNavigateToBarcodeScanner = { navController.navigate(LuminaRoute.CameraScannerRedesign.route) }
                        )
                    }
                    LuminaRoute.FoodDetail -> {
                        com.lumina.nutrition.feature.logging.detail.FoodDetailScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onFoodLogged = {
                                navController.navigate(LuminaRoute.Home.route) {
                                    popUpTo(LuminaRoute.Home.route) { inclusive = false }
                                }
                            }
                        )
                    }
                    LuminaRoute.CameraScannerRedesign, LuminaRoute.BarcodeScanner -> {
                        com.lumina.nutrition.feature.logging.scanner.CameraScannerScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { navController.navigate(LuminaRoute.FoodDetail.route) },
                            onNavigateToSearch = {
                                navController.navigate(LuminaRoute.FoodSearch.route) {
                                    popUpTo(LuminaRoute.CameraScannerRedesign.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    LuminaRoute.PhotoReview -> {
                        PhotoReviewScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onAnalysisComplete = { result ->
                                if (result.foods.size > 1) {
                                    navController.navigate(LuminaRoute.FoodResultMulti.route)
                                } else if (result.overallConfidence >= 0.50) {
                                    navController.navigate(LuminaRoute.FoodResult.route)
                                } else {
                                    navController.navigate(LuminaRoute.FoodClarify.route)
                                }
                            },
                            onNavigateToManualSearch = {
                                navController.navigate(LuminaRoute.FoodSearch.route) {
                                    popUpTo(LuminaRoute.PhotoReview.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    LuminaRoute.TextInput -> {
                        TextInputScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToReview = { navController.navigate(LuminaRoute.AiFoodReview.route) },
                            onNavigateToManualSearch = {
                                navController.navigate(LuminaRoute.FoodSearch.route) {
                                    popUpTo(LuminaRoute.TextInput.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    LuminaRoute.VoiceInput -> {
                        VoiceInputScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToReview = { navController.navigate(LuminaRoute.AiFoodReview.route) },
                            onNavigateToManualSearch = {
                                navController.navigate(LuminaRoute.FoodSearch.route) {
                                    popUpTo(LuminaRoute.VoiceInput.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    LuminaRoute.FoodResult -> {
                        FoodResultScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToMealConfirm = { navController.navigate(LuminaRoute.MealConfirm.route) },
                            sessionManager = sessionManager
                        )
                    }
                    LuminaRoute.FoodResultMulti -> {
                        FoodResultMultiScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToMealConfirm = { navController.navigate(LuminaRoute.MealConfirm.route) },
                            sessionManager = sessionManager
                        )
                    }
                    LuminaRoute.FoodClarify -> {
                        FoodClarifyScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToResult = { navController.navigate(LuminaRoute.FoodResult.route) },
                            onNavigateToSearch = { navController.navigate(LuminaRoute.FoodSearch.route) },
                            sessionManager = sessionManager
                        )
                    }
                    LuminaRoute.AiFoodReview -> {
                        AiFoodReviewScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToConfirm = { navController.navigate(LuminaRoute.MealConfirm.route) },
                            sessionManager = sessionManager
                        )
                    }
                    LuminaRoute.MealConfirm -> {
                        MealConfirmScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onMealLogged = {
                                navController.navigate(LuminaRoute.Home.route) {
                                    popUpTo(LuminaRoute.Home.route) { inclusive = false }
                                }
                            }
                        )
                    }
                    LuminaRoute.AiChat, LuminaRoute.AiAssistant, LuminaRoute.AiConfirmation -> {
                        AiChatScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    LuminaRoute.Home -> {
                        HomeScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(LuminaRoute.AddFoodSheet.route) },
                            onNavigateToMealDetail = { mealId ->
                                navController.navigate("${LuminaRoute.MealDetail.route}?mealId=$mealId")
                            },
                            onNavigateToAiCoach = { navController.navigate(LuminaRoute.AiChat.route) }
                        )
                    }
                    LuminaRoute.Diary -> {
                        DiaryScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(LuminaRoute.AddFoodSheet.route) },
                            onNavigateToMealDetail = { mealId ->
                                navController.navigate("${LuminaRoute.MealDetail.route}?mealId=$mealId")
                            }
                        )
                    }
                    LuminaRoute.MealDetail -> {
                        val mealId = it.arguments?.getString("mealId")?.toLongOrNull() ?: 1L
                        MealDetailScreen(
                            mealId = mealId,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAddFood = { navController.navigate(LuminaRoute.AddFoodSheet.route) },
                            onDuplicateSuccess = {
                                navController.navigate(LuminaRoute.Diary.route) {
                                    popUpTo(LuminaRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    LuminaRoute.Insights -> {
                        com.lumina.nutrition.feature.insights.InsightsScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(LuminaRoute.AddFoodSheet.route) },
                            onNavigateToWeightLog = { navController.navigate(LuminaRoute.WeightLog.route) }
                        )
                    }
                    LuminaRoute.WeightLog -> {
                        com.lumina.nutrition.feature.weight.WeightLogScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    else -> when (destination.flow) {
                        "Diary" -> DiaryScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(LuminaRoute.AddFoodSheet.route) },
                            onNavigateToMealDetail = { mealId ->
                                navController.navigate("${LuminaRoute.MealDetail.route}?mealId=$mealId")
                            }
                        )
                        else -> LoggingSkeletonScreen(destination, navigateToRoute)
                    }
                }
            }
        }
        composable(
            route = "${LuminaRoute.MealDetail.route}?mealId={mealId}",
            arguments = listOf(navArgument("mealId") { defaultValue = "1"; type = NavType.StringType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId")?.toLongOrNull() ?: 1L
            MealDetailScreen(
                mealId = mealId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFood = { navController.navigate(LuminaRoute.AddFoodSheet.route) },
                onDuplicateSuccess = {
                    navController.navigate(LuminaRoute.Diary.route) {
                        popUpTo(LuminaRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}