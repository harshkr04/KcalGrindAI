package com.kcalgrindai.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kcalgrindai.app.feature.aicoach.AiChatScreen
import com.kcalgrindai.app.feature.diary.DiaryScreen
import com.kcalgrindai.app.feature.diary.detail.MealDetailScreen
import com.kcalgrindai.app.feature.home.HomeScreen
import com.kcalgrindai.app.feature.insights.InsightsSkeletonScreen
import com.kcalgrindai.app.feature.logging.LoggingSkeletonScreen
import com.kcalgrindai.app.feature.logging.clarify.FoodClarifyScreen
import com.kcalgrindai.app.feature.logging.confirm.MealConfirmScreen
import com.kcalgrindai.app.feature.logging.photo.PhotoReviewScreen
import com.kcalgrindai.app.feature.logging.result.FoodResultMultiScreen
import com.kcalgrindai.app.feature.logging.result.FoodResultScreen
import com.kcalgrindai.app.feature.logging.review.AiFoodReviewScreen
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import com.kcalgrindai.app.feature.logging.text.TextInputScreen
import com.kcalgrindai.app.feature.logging.voice.VoiceInputScreen
import com.kcalgrindai.app.feature.onboarding.activity.ActivityLevelScreen
import com.kcalgrindai.app.feature.onboarding.allergies.AllergiesScreen
import com.kcalgrindai.app.feature.onboarding.body.BodyMetricsScreen
import com.kcalgrindai.app.feature.onboarding.breather.BreatherActivityScreen
import com.kcalgrindai.app.feature.onboarding.breather.BreatherGoalScreen
import com.kcalgrindai.app.feature.onboarding.breather.BreatherScienceScreen
import com.kcalgrindai.app.feature.onboarding.diet.DietPreferencesScreen
import com.kcalgrindai.app.feature.onboarding.goal.GoalSelectionScreen
import com.kcalgrindai.app.feature.onboarding.goalsetting.GoalSettingScreen
import com.kcalgrindai.app.feature.onboarding.permissions.PermissionSetupScreen
import com.kcalgrindai.app.feature.onboarding.personal.PersonalDetailsScreen
import com.kcalgrindai.app.feature.onboarding.planreveal.PlanRevealScreen
import com.kcalgrindai.app.feature.onboarding.target.CalorieTargetScreen
import com.kcalgrindai.app.feature.onboarding.welcome.WelcomeScreen
import com.kcalgrindai.app.feature.profile.ProfileScreen
import com.kcalgrindai.app.feature.splash.LaunchDestination
import com.kcalgrindai.app.feature.splash.LaunchViewModel

@Composable
fun KcalGrindNavHost(
    navController: NavHostController = rememberNavController(),
    launchViewModel: LaunchViewModel = hiltViewModel(),
    loggingNavViewModel: com.kcalgrindai.app.feature.logging.state.LoggingNavigationViewModel = hiltViewModel()
) {
    val sessionManager = loggingNavViewModel.sessionManager
    val destinations = remember { KcalGrindRoute.all }
    val routeMap = remember(destinations) { KcalGrindRoute.validatedRouteMap(destinations) }
    val launchDestination by launchViewModel.destination.collectAsState()

    val navigateToRoute: (String) -> Unit = { route ->
        navController.navigate(routeMap.getValue(route).route)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val onNavigateToTab: (String) -> Unit = { route ->
        if (currentRoute != route) {
            val popped = navController.popBackStack(route, inclusive = false)
            if (!popped) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    if (launchDestination is LaunchDestination.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val startRoute = if (launchDestination is LaunchDestination.Home) {
        KcalGrindRoute.Home.route
    } else {
        KcalGrindRoute.Welcome.route
    }

    val showBottomBar = currentRoute in setOf(
        KcalGrindRoute.Home.route,
        KcalGrindRoute.Diary.route,
        KcalGrindRoute.Insights.route,
        KcalGrindRoute.AiChat.route,
        KcalGrindRoute.AiAssistant.route,
        KcalGrindRoute.AiConfirmation.route,
        KcalGrindRoute.Profile.route
    )
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isKeyboardOpen = imeBottom > 0.dp

    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState),
        enterTransition = {
            fadeIn(animationSpec = tween(150))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(150))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(150))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(150))
        }
    ) {
        destinations.filter { it != KcalGrindRoute.CameraScannerRedesign && it != KcalGrindRoute.BarcodeScanner }.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    KcalGrindRoute.Welcome -> {
                        WelcomeScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.GoalSelection.route) },
                            onNavigateToAuth = { navController.navigate(KcalGrindRoute.Auth.route) }
                        )
                    }
                    KcalGrindRoute.Auth -> {
                        com.kcalgrindai.app.feature.auth.AuthScreen(
                            onAuthSuccess = {
                                launchViewModel.checkUserProfile()
                                navController.navigate(KcalGrindRoute.Home.route) {
                                    popUpTo(KcalGrindRoute.Welcome.route) { inclusive = true }
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.GoalSelection -> {
                        GoalSelectionScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.BreatherGoal.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.BreatherGoal -> {
                        BreatherGoalScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.DietPreferences.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.DietPreferences -> {
                        DietPreferencesScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.Allergies.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.Allergies -> {
                        AllergiesScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.PersonalDetails.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.PersonalDetails -> {
                        PersonalDetailsScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.ActivityLevel.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.ActivityLevel -> {
                        ActivityLevelScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.BreatherActivity.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.BreatherActivity -> {
                        BreatherActivityScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.BodyMetrics.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.BodyMetrics -> {
                        BodyMetricsScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.BreatherScience.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.BreatherScience -> {
                        BreatherScienceScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.CalorieTarget.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.CalorieTarget -> {
                        CalorieTargetScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.GoalSetting.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.GoalSetting -> {
                        GoalSettingScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.PlanReveal.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.PlanReveal -> {
                        PlanRevealScreen(
                            onNavigateNext = { navController.navigate(KcalGrindRoute.PermissionSetup.route) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.PermissionSetup -> {
                        PermissionSetupScreen(
                            onNavigateHome = {
                                navController.navigate(KcalGrindRoute.Home.route) {
                                    popUpTo(KcalGrindRoute.Welcome.route) { inclusive = true }
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.Profile -> {
                        ProfileScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
                            onNavigateToAuth = {
                                navController.navigate(KcalGrindRoute.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToSubscription = {
                                navController.navigate(KcalGrindRoute.Subscription.route)
                            }
                        )
                    }
                    KcalGrindRoute.Subscription -> {
                        com.kcalgrindai.app.feature.profile.subscription.SubscriptionScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    KcalGrindRoute.AddFoodSheet -> {
                        com.kcalgrindai.app.feature.logging.sheet.AddFoodSheetScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSearch = { navController.navigate(KcalGrindRoute.FoodSearch.route) },
                            onNavigateToBarcode = { navController.navigate(KcalGrindRoute.CameraScannerRedesign.route) },
                            onNavigateToPhoto = { navController.navigate("${KcalGrindRoute.CameraScannerRedesign.route}?mode=photo") },
                            onNavigateToVoice = { navController.navigate(KcalGrindRoute.VoiceInput.route) },
                            onNavigateToText = { navController.navigate(KcalGrindRoute.TextInput.route) }
                        )
                    }
                    KcalGrindRoute.FoodSearch -> {
                        com.kcalgrindai.app.feature.logging.search.FoodSearchScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { navController.navigate(KcalGrindRoute.FoodDetail.route) },
                            onNavigateToBarcodeScanner = { navController.navigate(KcalGrindRoute.CameraScannerRedesign.route) }
                        )
                    }
                    KcalGrindRoute.FoodDetail -> {
                        com.kcalgrindai.app.feature.logging.detail.FoodDetailScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onFoodLogged = {
                                navController.navigate(KcalGrindRoute.Home.route) {
                                    popUpTo(KcalGrindRoute.Home.route) { inclusive = false }
                                }
                            }
                        )
                    }
                    KcalGrindRoute.PhotoReview -> {
                        PhotoReviewScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToCamera = {
                                navController.navigate("camera_scanner_redesign?mode=photo") {
                                    popUpTo(KcalGrindRoute.PhotoReview.route) { inclusive = true }
                                }
                            },
                            onAnalysisComplete = { result ->
                                if (result.foods.size > 1) {
                                    navController.navigate(KcalGrindRoute.FoodResultMulti.route)
                                } else if (result.overallConfidence >= 0.50) {
                                    navController.navigate(KcalGrindRoute.FoodResult.route)
                                } else {
                                    navController.navigate(KcalGrindRoute.FoodClarify.route)
                                }
                            },
                            onNavigateToManualSearch = {
                                navController.navigate(KcalGrindRoute.FoodSearch.route) {
                                    popUpTo(KcalGrindRoute.PhotoReview.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    KcalGrindRoute.TextInput -> {
                        TextInputScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToReview = { navController.navigate(KcalGrindRoute.AiFoodReview.route) },
                            onNavigateToManualSearch = {
                                navController.navigate(KcalGrindRoute.FoodSearch.route) {
                                    popUpTo(KcalGrindRoute.TextInput.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    KcalGrindRoute.VoiceInput -> {
                        VoiceInputScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToReview = { navController.navigate(KcalGrindRoute.AiFoodReview.route) },
                            onNavigateToManualSearch = {
                                navController.navigate(KcalGrindRoute.FoodSearch.route) {
                                    popUpTo(KcalGrindRoute.VoiceInput.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    KcalGrindRoute.FoodResult -> {
                        FoodResultScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToMealConfirm = { navController.navigate(KcalGrindRoute.MealConfirm.route) },
                            sessionManager = sessionManager
                        )
                    }
                    KcalGrindRoute.FoodResultMulti -> {
                        FoodResultMultiScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToMealConfirm = { navController.navigate(KcalGrindRoute.MealConfirm.route) },
                            sessionManager = sessionManager
                        )
                    }
                    KcalGrindRoute.FoodClarify -> {
                        FoodClarifyScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToResult = { navController.navigate(KcalGrindRoute.FoodResult.route) },
                            onNavigateToSearch = { navController.navigate(KcalGrindRoute.FoodSearch.route) },
                            sessionManager = sessionManager
                        )
                    }
                    KcalGrindRoute.AiFoodReview -> {
                        AiFoodReviewScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToConfirm = { navController.navigate(KcalGrindRoute.MealConfirm.route) },
                            sessionManager = sessionManager
                        )
                    }
                    KcalGrindRoute.MealConfirm -> {
                        MealConfirmScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onMealLogged = {
                                navController.navigate(KcalGrindRoute.Home.route) {
                                    popUpTo(KcalGrindRoute.Home.route) { inclusive = false }
                                }
                            }
                        )
                    }
                    KcalGrindRoute.AiChat, KcalGrindRoute.AiAssistant, KcalGrindRoute.AiConfirmation -> {
                        AiChatScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToTab = onNavigateToTab,
                            onOpenAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
                            onNavigateToSubscription = { navController.navigate(KcalGrindRoute.Subscription.route) }
                        )
                    }
                    KcalGrindRoute.Home -> {
                        HomeScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { mealType ->
                                if (mealType != null) {
                                    sessionManager.setSelectedMealType(mealType)
                                }
                                navController.navigate(KcalGrindRoute.AddFoodSheet.route)
                            },
                            onNavigateToMealDetail = { mealId ->
                                navController.navigate("${KcalGrindRoute.MealDetail.route}?mealId=$mealId")
                            },
                            onNavigateToAiCoach = { onNavigateToTab(KcalGrindRoute.AiChat.route) },
                            onNavigateToProfile = { navController.navigate(KcalGrindRoute.Profile.route) }
                        )
                    }
                    KcalGrindRoute.Diary -> {
                        DiaryScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
                            onNavigateToMealDetail = { mealId ->
                                navController.navigate("${KcalGrindRoute.MealDetail.route}?mealId=$mealId")
                            }
                        )
                    }
                    KcalGrindRoute.MealDetail -> {
                        val mealId = it.arguments?.getString("mealId")?.toLongOrNull() ?: 1L
                        MealDetailScreen(
                            mealId = mealId,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
                            onDuplicateSuccess = {
                                navController.navigate(KcalGrindRoute.Diary.route) {
                                    popUpTo(KcalGrindRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    KcalGrindRoute.Insights -> {
                        com.kcalgrindai.app.feature.insights.InsightsScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
                            onNavigateToWeightLog = { navController.navigate(KcalGrindRoute.WeightLog.route) },
                            onNavigateToRecipeDetail = { recipeId ->
                                navController.navigate("${KcalGrindRoute.RecipeDetail.route}?recipeId=$recipeId")
                            }
                        )
                    }
                    KcalGrindRoute.RecipeDetail -> {
                        com.kcalgrindai.app.feature.recipe.RecipeDetailScreen(
                            recipeId = "",
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDiary = {
                                navController.navigate(KcalGrindRoute.Diary.route) {
                                    popUpTo(KcalGrindRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    KcalGrindRoute.WeightLog -> {
                        com.kcalgrindai.app.feature.weight.WeightLogScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    else -> when (destination.flow) {
                        "Diary" -> DiaryScreen(
                            onNavigateToTab = onNavigateToTab,
                            onNavigateToAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
                            onNavigateToMealDetail = { mealId ->
                                navController.navigate("${KcalGrindRoute.MealDetail.route}?mealId=$mealId")
                            }
                        )
                        else -> LoggingSkeletonScreen(destination, navigateToRoute)
                    }
                }
            }
        }
        composable(
            route = "${KcalGrindRoute.CameraScannerRedesign.route}?mode={mode}",
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "barcode"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "barcode"
            com.kcalgrindai.app.feature.logging.scanner.CameraScannerScreen(
                initialMode = mode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate(KcalGrindRoute.FoodDetail.route) },
                onNavigateToPhotoReview = { navController.navigate(KcalGrindRoute.PhotoReview.route) },
                onNavigateToSearch = {
                    navController.navigate(KcalGrindRoute.FoodSearch.route) {
                        popUpTo(KcalGrindRoute.CameraScannerRedesign.route) { inclusive = true }
                    }
                },
                photoDraftStore = loggingNavViewModel.photoDraftStore,
                sessionManager = sessionManager
            )
        }
        composable(KcalGrindRoute.BarcodeScanner.route) {
            com.kcalgrindai.app.feature.logging.scanner.CameraScannerScreen(
                initialMode = "barcode",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate(KcalGrindRoute.FoodDetail.route) },
                onNavigateToPhotoReview = { navController.navigate(KcalGrindRoute.PhotoReview.route) },
                onNavigateToSearch = {
                    navController.navigate(KcalGrindRoute.FoodSearch.route) {
                        popUpTo(KcalGrindRoute.BarcodeScanner.route) { inclusive = true }
                    }
                },
                photoDraftStore = loggingNavViewModel.photoDraftStore,
                sessionManager = sessionManager
            )
        }
        composable(
            route = "${KcalGrindRoute.MealDetail.route}?mealId={mealId}",
            arguments = listOf(navArgument("mealId") { defaultValue = "1"; type = NavType.StringType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId")?.toLongOrNull() ?: 1L
            MealDetailScreen(
                mealId = mealId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
                onDuplicateSuccess = {
                    navController.navigate(KcalGrindRoute.Diary.route) {
                        popUpTo(KcalGrindRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = "${KcalGrindRoute.RecipeDetail.route}?recipeId={recipeId}",
            arguments = listOf(navArgument("recipeId") { defaultValue = ""; type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            com.kcalgrindai.app.feature.recipe.RecipeDetailScreen(
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDiary = {
                    navController.navigate(KcalGrindRoute.Diary.route) {
                        popUpTo(KcalGrindRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }

    if (showBottomBar && !isKeyboardOpen) {
        val activeTabRoute = when (currentRoute) {
            KcalGrindRoute.AiAssistant.route, KcalGrindRoute.AiConfirmation.route -> KcalGrindRoute.AiChat.route
            else -> currentRoute ?: KcalGrindRoute.Home.route
        }
        KcalGrindBottomNavigationBar(
            currentRoute = activeTabRoute,
            onNavigateToTab = onNavigateToTab,
            onOpenAddFood = { navController.navigate(KcalGrindRoute.AddFoodSheet.route) },
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
}