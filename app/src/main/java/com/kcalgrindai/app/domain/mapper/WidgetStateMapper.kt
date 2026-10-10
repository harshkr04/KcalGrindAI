package com.kcalgrindai.app.domain.mapper

import com.kcalgrindai.app.domain.model.MealWithFoodItems
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.SproutlingMood
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.model.WidgetStatusType
import com.kcalgrindai.app.domain.model.WidgetUiState
import java.text.NumberFormat
import java.time.Clock
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class WidgetStateMapper @Inject constructor(
    private val clock: Clock
) {

    fun getCurrentDateString(): String {
        return LocalDate.now(clock).toString()
    }

    fun map(
        goal: NutritionGoal?,
        mealsWithItems: List<MealWithFoodItems>,
        waterTotalMl: Int,
        streakDays: Int,
        profile: UserProfile?,
        isPrivacyEnabled: Boolean,
        isLoading: Boolean = false,
        isLoggedIn: Boolean = true
    ): WidgetUiState {
        if (!isLoggedIn) {
            return WidgetUiState.LoggedOut.copy(
                lastUpdatedEpochMs = clock.millis()
            )
        }

        if (isLoading) {
            return WidgetUiState.Loading.copy(
                isPrivacyEnabled = isPrivacyEnabled,
                lastUpdatedEpochMs = clock.millis()
            )
        }

        if (profile == null || goal == null || goal.calories <= 0) {
            return WidgetUiState(
                targetCalories = goal?.calories ?: 2000,
                statusType = WidgetStatusType.ONBOARDING_INCOMPLETE,
                primaryHeadline = "Finish setup",
                subHeadline = "Set your daily goals to begin",
                sproutlingMood = SproutlingMood.WAVING,
                streakDays = streakDays,
                isPrivacyEnabled = isPrivacyEnabled,
                lastUpdatedEpochMs = clock.millis()
            )
        }

        if (isPrivacyEnabled) {
            return WidgetUiState(
                targetCalories = 0,
                consumedCalories = 0,
                remainingCalories = 0,
                progressPercent = 0,
                energyBlocksFilled = 0,
                proteinGrams = 0.0,
                targetProteinGrams = 0.0,
                proteinFraction = 0.0f,
                carbsGrams = 0.0,
                targetCarbsGrams = 0.0,
                carbsFraction = 0.0f,
                fatGrams = 0.0,
                targetFatGrams = 0.0,
                fatFraction = 0.0f,
                waterMl = 0,
                waterGlasses = 0,
                streakDays = streakDays,
                statusType = WidgetStatusType.PRIVACY,
                primaryHeadline = "•••• kcal left",
                subHeadline = "•••• / ••••",
                sproutlingMood = SproutlingMood.SECURE,
                isPrivacyEnabled = true,
                lastUpdatedEpochMs = clock.millis()
            )
        }

        val targetCalories = goal.calories
        val targetProtein = goal.proteinG
        val targetCarbs = goal.carbsG
        val targetFat = goal.fatG

        val validMeals = mealsWithItems.filter { it.items.isNotEmpty() }
        val consumedCalories = validMeals.sumOf { it.items.sumOf { item -> item.calories } }
        val consumedProtein = validMeals.sumOf { it.items.sumOf { item -> item.proteinG } }
        val consumedCarbs = validMeals.sumOf { it.items.sumOf { item -> item.carbsG } }
        val consumedFat = validMeals.sumOf { it.items.sumOf { item -> item.fatG } }

        val consumedInt = consumedCalories.roundToInt()
        val remainingCalories = targetCalories - consumedInt
        val progressPercent = if (targetCalories > 0) {
            ((consumedCalories / targetCalories.toDouble()) * 100.0).roundToInt()
        } else {
            0
        }

        val energyBlocksFilled = when {
            consumedCalories <= 0.0 -> 0
            consumedCalories >= targetCalories -> 10
            else -> ((consumedCalories / targetCalories.toDouble()) * 10.0).roundToInt().coerceIn(1, 9)
        }

        val proteinFraction = if (targetProtein > 0) {
            min(1.0f, max(0.0f, (consumedProtein / targetProtein).toFloat()))
        } else 0.0f

        val carbsFraction = if (targetCarbs > 0) {
            min(1.0f, max(0.0f, (consumedCarbs / targetCarbs).toFloat()))
        } else 0.0f

        val fatFraction = if (targetFat > 0) {
            min(1.0f, max(0.0f, (consumedFat / targetFat).toFloat()))
        } else 0.0f

        val numberFormat = NumberFormat.getIntegerInstance(Locale.US)

        val (statusType, headline, subhead, mood) = when {
            isPrivacyEnabled -> {
                Quadruple(
                    WidgetStatusType.PRIVACY,
                    "•••• kcal left",
                    "•••• / ••••",
                    SproutlingMood.SECURE
                )
            }
            consumedInt <= 0 -> {
                Quadruple(
                    WidgetStatusType.EMPTY,
                    "${numberFormat.format(targetCalories)} kcal left",
                    "Ready to log today",
                    SproutlingMood.RESTING
                )
            }
            remainingCalories == 0 -> {
                Quadruple(
                    WidgetStatusType.GOAL_REACHED,
                    "0 kcal left",
                    "Daily goal reached",
                    SproutlingMood.ENERGIZED
                )
            }
            remainingCalories < 0 -> {
                val overAmount = consumedInt - targetCalories
                Quadruple(
                    WidgetStatusType.OVER_GOAL,
                    "${numberFormat.format(overAmount)} kcal over",
                    "Daily budget exceeded",
                    SproutlingMood.CONTENT
                )
            }
            else -> {
                Quadruple(
                    WidgetStatusType.NORMAL,
                    "${numberFormat.format(remainingCalories)} kcal left",
                    "${numberFormat.format(consumedInt)} / ${numberFormat.format(targetCalories)}",
                    SproutlingMood.ACTIVE
                )
            }
        }

        return WidgetUiState(
            targetCalories = targetCalories,
            consumedCalories = consumedInt,
            remainingCalories = remainingCalories,
            progressPercent = progressPercent,
            energyBlocksFilled = energyBlocksFilled,
            proteinGrams = consumedProtein,
            targetProteinGrams = targetProtein,
            proteinFraction = proteinFraction,
            carbsGrams = consumedCarbs,
            targetCarbsGrams = targetCarbs,
            carbsFraction = carbsFraction,
            fatGrams = consumedFat,
            targetFatGrams = targetFat,
            fatFraction = fatFraction,
            waterMl = waterTotalMl,
            waterGlasses = waterTotalMl / 250,
            streakDays = streakDays,
            statusType = statusType,
            primaryHeadline = headline,
            subHeadline = subhead,
            sproutlingMood = mood,
            isPrivacyEnabled = isPrivacyEnabled,
            lastUpdatedEpochMs = clock.millis()
        )
    }

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
