package com.kcalgrindai.app.feature.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kcalgrindai.app.R
import com.kcalgrindai.app.domain.model.SproutlingMood
import com.kcalgrindai.app.domain.model.WidgetStatusType
import com.kcalgrindai.app.domain.model.WidgetUiState
import com.kcalgrindai.app.feature.widget.action.LogWaterActionCallback
import com.kcalgrindai.app.feature.widget.action.WidgetActions
import com.kcalgrindai.app.feature.widget.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.roundToInt

class KcalGrindAppWidget : GlanceAppWidget() {

    companion object {
        val COMPACT_2X2 = DpSize(110.dp, 110.dp)
        val MINIMAL_4X1 = DpSize(250.dp, 48.dp)
        val EXPANDED_4X2 = DpSize(250.dp, 110.dp)

        // Color tokens matching Docs/design/WIDGET_SPEC.md
        val SurfaceColor = ColorProvider(day = Color(0xFFB0F1CC), night = Color(0xFF0B2B1D))
        val PrimaryTextColor = ColorProvider(day = Color(0xFF191C1C), night = Color(0xFFEFF1F0))
        val SecondaryTextColor = ColorProvider(day = Color(0xFF404943), night = Color(0xFFA8D1BC))
        val EnergyActiveColor = ColorProvider(day = Color(0xFF0F5238), night = Color(0xFF95D4B3))
        val EnergyTrackColor = ColorProvider(day = Color(0xFFC8F7DC), night = Color(0xFF051A10))

        val ActionAddColor = ColorProvider(day = Color(0xFF0F5238), night = Color(0xFF95D4B3))
        val ActionAddIconColor = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF0A2619))
        val ActionAiColor = ColorProvider(day = Color(0xFF313C9F), night = Color(0xFFBDC2FF))
        val ActionWaterColor = ColorProvider(day = Color(0xFF2B694D), night = Color(0xFFA8E7C5))

        val MacroProteinColor = ColorProvider(day = Color(0xFF195E3E), night = Color(0xFF7EC89F))
        val MacroCarbsColor = ColorProvider(day = Color(0xFF2D6A4F), night = Color(0xFF95D4B3))
        val MacroFatColor = ColorProvider(day = Color(0xFF407855), night = Color(0xFF5BA678))
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(COMPACT_2X2, MINIMAL_4X1, EXPANDED_4X2)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadWidgetState(context)
        provideContent {
            val size = LocalSize.current
            val isWide = size.width >= 240.dp
            val isTall = size.height >= 100.dp

            val rootModifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ImageProvider(R.drawable.bg_widget_container))
                .cornerRadius(16.dp)
                .clickable(
                    actionStartActivity(
                        if (state.statusType == WidgetStatusType.ONBOARDING_INCOMPLETE) {
                            WidgetActions.goalSetupIntent(context)
                        } else {
                            WidgetActions.openAppIntent(context)
                        }
                    )
                )

            Box(modifier = rootModifier) {
                when {
                    isWide && isTall -> Expanded4x2Layout(context, state)
                    isWide && !isTall -> Minimal4x1Layout(context, state)
                    else -> Compact2x2Layout(context, state)
                }
            }
        }
    }

    internal suspend fun loadWidgetState(entryPoint: WidgetEntryPoint): WidgetUiState {
        val profile = entryPoint.userProfileRepository().getProfile()
        if (profile == null) {
            return WidgetUiState.LoggedOut
        }

        val authUser = entryPoint.authRepository().getCurrentUser()
        if (profile.firebaseUid != null && (authUser == null || authUser.uid != profile.firebaseUid)) {
            return WidgetUiState.LoggedOut
        }

        val mapper = entryPoint.widgetStateMapper()
        val today = mapper.getCurrentDateString()
        val goal = entryPoint.nutritionGoalRepository().getLatestGoal()
        val meals = entryPoint.mealLogRepository().observeMealsWithItemsByDate(today).firstOrNull() ?: emptyList()
        val waterLogs = entryPoint.waterLogRepository().getWaterLogsByDate(today)
        val waterTotal = waterLogs.sumOf { it.amountMl }
        val streak = entryPoint.mealLogRepository().getStreakDays()
        val isPrivacy = entryPoint.userPreferencesRepository().widgetPrivacyEnabled.firstOrNull() ?: false

        return mapper.map(
            goal = goal,
            mealsWithItems = meals,
            waterTotalMl = waterTotal,
            streakDays = streak,
            profile = profile,
            isPrivacyEnabled = isPrivacy,
            isLoading = false,
            isLoggedIn = true
        )
    }

    private suspend fun loadWidgetState(context: Context): WidgetUiState {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            loadWidgetState(entryPoint)
        } catch (e: Exception) {
            WidgetUiState.Loading
        }
    }

    @androidx.compose.runtime.Composable
    private fun Compact2x2Layout(context: Context, state: WidgetUiState) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Top Row: Sproutling Avatar + App Title + Quick Add Button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(getSproutlingDrawable(state.sproutlingMood)),
                    contentDescription = "Sproutling",
                    modifier = GlanceModifier.size(36.dp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Kcal Grind",
                        style = TextStyle(
                            color = PrimaryTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    if (state.streakDays > 0) {
                        Text(
                            text = "${state.streakDays}d streak",
                            style = TextStyle(
                                color = SecondaryTextColor,
                                fontSize = 9.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .background(ImageProvider(R.drawable.bg_widget_action_primary))
                        .clickable(actionStartActivity(WidgetActions.addMealIntent(context))),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_add),
                        contentDescription = "Add meal",
                        modifier = GlanceModifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Main Nutrition Readout
            Text(
                text = state.primaryHeadline,
                style = TextStyle(
                    color = PrimaryTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = state.subHeadline,
                style = TextStyle(
                    color = SecondaryTextColor,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Stepped Pixel Energy Progress Track (10 Discrete Blocks)
            SteppedPixelEnergyTrack(state.energyBlocksFilled)
        }
    }

    @androidx.compose.runtime.Composable
    private fun Minimal4x1Layout(context: Context, state: WidgetUiState) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(getSproutlingDrawable(state.sproutlingMood)),
                contentDescription = "Sproutling",
                modifier = GlanceModifier.size(34.dp)
            )
            Spacer(modifier = GlanceModifier.width(10.dp))

            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.primaryHeadline,
                    style = TextStyle(
                        color = PrimaryTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                SteppedPixelEnergyTrack(state.energyBlocksFilled)
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // Quick Actions: Add Meal (+) and Quick Water (+250ml)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = GlanceModifier
                        .size(30.dp)
                        .background(ImageProvider(R.drawable.bg_widget_action_water))
                        .clickable(actionRunCallback<LogWaterActionCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_water),
                        contentDescription = "Quick water",
                        modifier = GlanceModifier.size(16.dp)
                    )
                }
                Spacer(modifier = GlanceModifier.width(6.dp))
                Box(
                    modifier = GlanceModifier
                        .size(30.dp)
                        .background(ImageProvider(R.drawable.bg_widget_action_primary))
                        .clickable(actionStartActivity(WidgetActions.addMealIntent(context))),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_add),
                        contentDescription = "Add meal",
                        modifier = GlanceModifier.size(16.dp)
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun Expanded4x2Layout(context: Context, state: WidgetUiState) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Sproutling, Calorie Readout, Energy Track
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(getSproutlingDrawable(state.sproutlingMood)),
                        contentDescription = "Sproutling",
                        modifier = GlanceModifier.size(40.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Column {
                        Text(
                            text = "Kcal Grind AI",
                            style = TextStyle(
                                color = PrimaryTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (state.streakDays > 0) "${state.streakDays} Day Streak" else "Living Companion",
                            style = TextStyle(
                                color = SecondaryTextColor,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Column {
                    Text(
                        text = state.primaryHeadline,
                        style = TextStyle(
                            color = PrimaryTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = state.subHeadline,
                        style = TextStyle(
                            color = SecondaryTextColor,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                SteppedPixelEnergyTrack(state.energyBlocksFilled)
            }

            Spacer(modifier = GlanceModifier.width(16.dp))

            // Right Column: Quick Action Bar + Macronutrient Progress
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.Top
            ) {
                // Quick Action Buttons
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Camera Scan
                    Box(
                        modifier = GlanceModifier
                            .size(32.dp)
                            .background(ImageProvider(R.drawable.bg_widget_action_ai))
                            .clickable(actionStartActivity(WidgetActions.cameraScanIntent(context))),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_camera),
                            contentDescription = "AI Camera scan",
                            modifier = GlanceModifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Water Quick Log (+250ml)
                    Box(
                        modifier = GlanceModifier
                            .size(32.dp)
                            .background(ImageProvider(R.drawable.bg_widget_action_water))
                            .clickable(actionRunCallback<LogWaterActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_water),
                            contentDescription = "Quick water",
                            modifier = GlanceModifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Add Meal (+)
                    Box(
                        modifier = GlanceModifier
                            .size(32.dp)
                            .background(ImageProvider(R.drawable.bg_widget_action_primary))
                            .clickable(actionStartActivity(WidgetActions.addMealIntent(context))),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_add),
                            contentDescription = "Add meal",
                            modifier = GlanceModifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Macronutrient Trackers
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    MacroRow(
                        name = "Protein",
                        value = if (state.isPrivacyEnabled) "•••• g" else "${state.proteinGrams.roundToInt()}g",
                        fraction = state.proteinFraction,
                        color = MacroProteinColor
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    MacroRow(
                        name = "Carbs",
                        value = if (state.isPrivacyEnabled) "•••• g" else "${state.carbsGrams.roundToInt()}g",
                        fraction = state.carbsFraction,
                        color = MacroCarbsColor
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    MacroRow(
                        name = "Fat",
                        value = if (state.isPrivacyEnabled) "•••• g" else "${state.fatGrams.roundToInt()}g",
                        fraction = state.fatFraction,
                        color = MacroFatColor
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun MacroRow(
        name: String,
        value: String,
        fraction: Float,
        color: androidx.glance.unit.ColorProvider
    ) {
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = TextStyle(
                        color = SecondaryTextColor,
                        fontSize = 10.sp
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = value,
                    style = TextStyle(
                        color = PrimaryTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = GlanceModifier.height(2.dp))
            LinearProgressIndicator(
                progress = fraction,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .cornerRadius(2.dp),
                color = color,
                backgroundColor = EnergyTrackColor
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun SteppedPixelEnergyTrack(blocksFilled: Int) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 10) {
                val isFilled = i < blocksFilled
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(6.dp)
                        .padding(horizontal = 1.dp)
                        .background(if (isFilled) EnergyActiveColor else EnergyTrackColor)
                        .cornerRadius(2.dp)
                ) {}
            }
        }
    }

    private fun getSproutlingDrawable(mood: SproutlingMood): Int {
        return when (mood) {
            SproutlingMood.RESTING -> R.drawable.ic_sproutling_resting
            SproutlingMood.ACTIVE -> R.drawable.ic_sproutling_active
            SproutlingMood.ENERGIZED -> R.drawable.ic_sproutling_energized
            SproutlingMood.CONTENT -> R.drawable.ic_sproutling_content
            SproutlingMood.WAVING -> R.drawable.ic_sproutling_waving
            SproutlingMood.LOADING -> R.drawable.ic_sproutling_loading
            SproutlingMood.SECURE -> R.drawable.ic_sproutling_secure
        }
    }
}
