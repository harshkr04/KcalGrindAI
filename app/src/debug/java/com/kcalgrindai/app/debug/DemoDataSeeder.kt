package com.kcalgrindai.app.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.kcalgrindai.app.BuildConfig
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.ConversationEntity
import com.kcalgrindai.app.data.local.entity.FoodLogItemEntity
import com.kcalgrindai.app.data.local.entity.MealLogEntity
import com.kcalgrindai.app.data.local.entity.MessageEntity
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import com.kcalgrindai.app.data.local.entity.UserProfileEntity
import com.kcalgrindai.app.data.local.entity.WaterLogEntity
import com.kcalgrindai.app.data.local.seed.IndianFoodDataset
import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import com.kcalgrindai.app.feature.widget.util.GlanceWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Debug-only Demo Data Seeder for KcalGrind.
 * STRICTLY guarded: Never executes in release builds.
 */
object DemoDataSeeder {

    private const val TAG = "DemoDataSeeder"
    const val ACTION_SEED_DEMO_DATA = "com.kcalgrindai.app.action.SEED_DEMO_DATA"

    /**
     * Seeds the demo profile, target, daily meals, water, streak, and coach conversation.
     * Guaranteed no-op in release builds via BuildConfig.DEBUG.
     */
    suspend fun seed(context: Context): Boolean {
        if (!BuildConfig.DEBUG) {
            Log.w(TAG, "Refusing to seed demo data: Not a DEBUG build.")
            return false
        }

        try {
            Log.i(TAG, "Seeding demo data for KcalGrind...")
            val db = Room.databaseBuilder(
                context.applicationContext,
                KcalGrindDatabase::class.java,
                KcalGrindDatabase.DATABASE_NAME
            ).fallbackToDestructiveMigration().build()

            val userProfileDao = db.userProfileDao()
            val nutritionGoalDao = db.nutritionGoalDao()
            val foodDao = db.foodDao()
            val mealLogDao = db.mealLogDao()
            val foodLogItemDao = db.foodLogItemDao()
            val waterLogDao = db.waterLogDao()
            val conversationDao = db.conversationDao()
            val messageDao = db.messageDao()

            // 1. Seed base IFCT foods if table is empty
            if (foodDao.searchFoods("", 5).isEmpty()) {
                Log.i(TAG, "Populating IFCT base foods...")
                foodDao.insertFoods(IndianFoodDataset.foods)
            }

            // 2. Demo User Profile
            // Profile: Age 28, Height 178cm, Weight 78kg, Goal Weight 72kg, Moderate Activity, Lose Goal
            val now = System.currentTimeMillis()
            val existingProfile = userProfileDao.getProfile()
            val profileId = if (existingProfile != null) {
                userProfileDao.updateProfile(
                    existingProfile.copy(
                        firstName = "Alex",
                        age = 28,
                        heightCm = 178.0,
                        weightKg = 78.0,
                        goalWeightKg = 72.0,
                        goal = "LOSE",
                        activityLevel = "MODERATE",
                        units = "METRIC",
                        updatedAt = now
                    )
                )
                existingProfile.id
            } else {
                userProfileDao.insertProfile(
                    UserProfileEntity(
                        id = 1L,
                        firebaseUid = "demo_user_local",
                        email = "demo@kcalgrind.app",
                        firstName = "Alex",
                        goal = "LOSE",
                        units = "METRIC",
                        age = 28,
                        heightCm = 178.0,
                        weightKg = 78.0,
                        goalWeightKg = 72.0,
                        activityLevel = "MODERATE",
                        dietTags = emptyList(),
                        allergies = emptyList(),
                        targetsSource = "CALCULATED",
                        createdAt = now - 7 * 86400000L,
                        updatedAt = now
                    )
                )
            }

            // 3. Nutrition Goal: Derived deterministically from NutritionCalculator.calculate()
            // BMR = 10*78 + 6.25*178 - 5*28 - 78 = 1,674.5 -> 1,675.0 kcal
            // TDEE = 1,674.5 * 1.55 (MODERATE) = 2,595.475 -> 2,595.0 kcal
            // Deficit = GoalType.LOSE.delta = -450 kcal
            // Daily Calorie Target = round((2,595.475 - 450) / 10) * 10 = 2,150 kcal
            // Macros: 140g Protein (1.8g/kg), 247g Carbs, 67g Fat (28% of calories)
            // Macro Calorie Sum = 140*4 + 247*4 + 67*9 = 560 + 988 + 603 = 2,151 kcal (~2,150 kcal)
            val calcResult = NutritionCalculator.calculate(
                weightKg = 78.0,
                heightCm = 178.0,
                age = 28,
                goal = GoalType.LOSE,
                activity = ActivityLevel.MODERATE,
                goalWeightKg = 72.0,
                sex = "male"
            )

            nutritionGoalDao.upsertGoal(
                NutritionGoalEntity(
                    id = 1L,
                    userId = profileId,
                    calories = calcResult.calories,
                    proteinG = calcResult.proteinG,
                    carbsG = calcResult.carbsG,
                    fatG = calcResult.fatG,
                    waterLiters = calcResult.waterLiters,
                    waterGlasses = calcResult.waterGlasses,
                    bmr = calcResult.bmr,
                    tdee = calcResult.tdee,
                    isCustom = false,
                    updatedAt = now
                )
            )

            // 4. Clear existing meal and water logs for a pristine demo state
            val today = LocalDate.now()
            val todayStr = today.toString()
            val existingTodayMeals = mealLogDao.getMealLogsByDate(todayStr)
            for (m in existingTodayMeals) {
                mealLogDao.deleteMealLog(m)
            }
            waterLogDao.deleteWaterLogsByDate(todayStr)

            // Look up bundled curated entries; unbundled entries are flagged as demo custom foods
            val dalFood = foodDao.getFoodByExternalId(IndianFoodDataset.DATASET_SOURCE_RECIPE, "IFCT_D01") // Moong Dal Tadka
            val paneerFood = foodDao.getFoodByExternalId(IndianFoodDataset.DATASET_SOURCE_RECIPE, "IFCT_P06") // Paneer Bhurji

            // 5. Today's Meals (Total = 1,623 kcal, 120g P / 180g C / 47g F):
            // Breakfast: Dal tadka & roti 420 kcal (16P / 64C / 11F) - Linked to IFCT_D01
            val breakfastMealId = mealLogDao.insertMealLog(
                MealLogEntity(
                    date = todayStr,
                    mealType = "BREAKFAST",
                    totalCalories = 420.0,
                    loggedAt = now - 14400000L,
                    source = "manual",
                    synced = false
                )
            )
            foodLogItemDao.insertItem(
                FoodLogItemEntity(
                    mealLogId = breakfastMealId,
                    foodId = dalFood?.id,
                    name = "Dal tadka & roti",
                    brand = dalFood?.brand ?: "Curated Indian Recipe Estimate",
                    servingDescription = "Yellow moong dal tadka (150g) + 2 whole wheat rotis",
                    servingGrams = 220.0,
                    calories = 420.0,
                    proteinG = 16.0,
                    carbsG = 64.0,
                    fatG = 11.0,
                    fiberG = 6.9,
                    source = if (dalFood != null) "curated_recipe" else "USER_ENTERED",
                    confirmed = true
                )
            )

            // Lunch: Chicken breast & rice 540 kcal (52P / 58C / 12F) - Demo Custom Food
            val lunchMealId = mealLogDao.insertMealLog(
                MealLogEntity(
                    date = todayStr,
                    mealType = "LUNCH",
                    totalCalories = 540.0,
                    loggedAt = now - 10800000L,
                    source = "manual",
                    synced = false
                )
            )
            foodLogItemDao.insertItem(
                FoodLogItemEntity(
                    mealLogId = lunchMealId,
                    foodId = null,
                    name = "Chicken breast & rice",
                    brand = "Demo Custom Food",
                    servingDescription = "Skinless chicken breast (180g) + Steamed basmati rice (200g)",
                    servingGrams = 380.0,
                    calories = 540.0,
                    proteinG = 52.0,
                    carbsG = 58.0,
                    fatG = 12.0,
                    fiberG = 1.4,
                    source = "USER_ENTERED",
                    confirmed = true
                )
            )

            // Dinner: Paneer bhurji & paratha 380 kcal (24P / 34C / 16F) - Linked to IFCT_P06
            val dinnerMealId = mealLogDao.insertMealLog(
                MealLogEntity(
                    date = todayStr,
                    mealType = "DINNER",
                    totalCalories = 380.0,
                    loggedAt = now - 3600000L,
                    source = "manual",
                    synced = false
                )
            )
            foodLogItemDao.insertItem(
                FoodLogItemEntity(
                    mealLogId = dinnerMealId,
                    foodId = paneerFood?.id,
                    name = "Paneer bhurji & paratha",
                    brand = paneerFood?.brand ?: "Curated Indian Recipe Estimate",
                    servingDescription = "Fresh paneer bhurji (150g) + 1 plain paratha",
                    servingGrams = 210.0,
                    calories = 380.0,
                    proteinG = 24.0,
                    carbsG = 34.0,
                    fatG = 16.0,
                    fiberG = 4.2,
                    source = if (paneerFood != null) "curated_recipe" else "USER_ENTERED",
                    confirmed = true
                )
            )

            // Snack: Whey protein shake with banana & peanut butter 283 kcal (28P / 24C / 8F) - Demo Custom Food
            val snackMealId = mealLogDao.insertMealLog(
                MealLogEntity(
                    date = todayStr,
                    mealType = "SNACK",
                    totalCalories = 283.0,
                    loggedAt = now - 7200000L,
                    source = "manual",
                    synced = false
                )
            )
            foodLogItemDao.insertItem(
                FoodLogItemEntity(
                    mealLogId = snackMealId,
                    foodId = null,
                    name = "Whey protein shake with banana & peanut butter",
                    brand = "Demo Custom Food",
                    servingDescription = "1 scoop whey protein isolate (30g) + 1 small banana (90g) + 1 tbsp peanut butter (12g)",
                    servingGrams = 132.0,
                    calories = 283.0,
                    proteinG = 28.0,
                    carbsG = 24.0,
                    fatG = 8.0,
                    fiberG = 3.0,
                    source = "USER_ENTERED",
                    confirmed = true
                )
            )

            // 6. Water Log: 1,250 ml
            waterLogDao.insertWaterLog(
                WaterLogEntity(
                    date = todayStr,
                    amountMl = 1250,
                    loggedAt = now - 1800000L
                )
            )

            val weightEntryDao = db.weightEntryDao()

            // Today's weight entry
            weightEntryDao.insertWeightEntry(
                com.kcalgrindai.app.data.local.entity.WeightEntryEntity(
                    weightKg = 78.0,
                    date = todayStr,
                    note = "Morning weigh-in",
                    loggedAt = now - 28800000L
                )
            )

            // 7. Seed Previous 6 Days for a complete 7-day History
            val pastMealProfiles = listOf(
                Triple(2120.0, 138.0, Pair(240.0, 67.0)),
                Triple(2080.0, 132.0, Pair(238.0, 66.0)),
                Triple(2150.0, 140.0, Pair(245.0, 68.0)),
                Triple(2010.0, 128.0, Pair(230.0, 64.0)),
                Triple(2140.0, 139.0, Pair(244.0, 67.0)),
                Triple(2090.0, 135.0, Pair(236.0, 66.0))
            )

            for (i in 1..6) {
                val pastDate = today.minusDays(i.toLong()).toString()
                val pastTs = now - (i * 86400000L)
                val (cals, protein, carbsFat) = pastMealProfiles[i - 1]
                val (carbs, fat) = carbsFat

                if (mealLogDao.getMealLogsByDate(pastDate).isEmpty()) {
                    val pastMealId = mealLogDao.insertMealLog(
                        MealLogEntity(
                            date = pastDate,
                            mealType = "LUNCH",
                            totalCalories = cals,
                            loggedAt = pastTs,
                            source = "manual",
                            synced = false
                        )
                    )
                    foodLogItemDao.insertItem(
                        FoodLogItemEntity(
                            mealLogId = pastMealId,
                            name = "Daily Balanced Meal",
                            brand = "Curated Indian Recipe Estimate",
                            servingDescription = "Standard wholesome Indian meal",
                            servingGrams = 400.0,
                            calories = cals,
                            proteinG = protein,
                            carbsG = carbs,
                            fatG = fat,
                            fiberG = 14.0,
                            source = "curated_recipe",
                            confirmed = true
                        )
                    )
                }

                // Water log for past days
                waterLogDao.insertWaterLog(
                    WaterLogEntity(
                        date = pastDate,
                        amountMl = 2200,
                        loggedAt = pastTs + 3600000L
                    )
                )

                // Weight log for past days
                val pastWeight = 78.0 + (i * 0.1)
                weightEntryDao.insertWeightEntry(
                    com.kcalgrindai.app.data.local.entity.WeightEntryEntity(
                        weightKg = pastWeight,
                        date = pastDate,
                        note = "Morning weigh-in",
                        loggedAt = pastTs
                    )
                )
            }

            // 8. Coach Conversation: Live production response from Gemini matching 40g protein within 527 kcal
            val existingConvs = conversationDao.getAllConversations()
            for (c in existingConvs) {
                messageDao.deleteMessagesForConversation(c.id)
                conversationDao.deleteConversation(c)
            }
            val convId = conversationDao.insertConversation(
                ConversationEntity(
                    id = 1L,
                    startedAt = now - 3600000L,
                    lastMessageAt = now
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "user",
                    text = "What should I eat for dinner with 40g protein?",
                    createdAt = now - 60000L
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "assistant",
                    text = "You've had a great day so far and have 527 calories left in your budget. Hitting 40g of protein with that remaining allowance is totally doable!\n\nHere is a wholesome, high-protein Indian dinner option that fits your exact goals:\n\n• Tandoori Chicken Breast (180g) with a side of yellow Moong Dal (1/2 cup) & a fresh cucumber-tomato salad:\n  - Calories: ~420 kcal\n  - Protein: ~43g (fits well under your 527 kcal budget while exceeding 40g protein)\n\nChicken breast is a lean powerhouse, and pairing it with dal keeps you full and within your calorie deficit for the day!",
                    createdAt = now
                )
            )

            // 9. Configure User Preferences: System Dark Mode enabled
            val prefs = context.getSharedPreferences("kcalgrind_user_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("theme_mode", "DARK")
                .putBoolean("dark_mode_enabled", true)
                .putBoolean("widget_promo_dismissed", true)
                .putBoolean("widget_privacy_enabled", false)
                .apply()

            // 10. Request Widget Update
            try {
                GlanceWidgetUpdater(context.applicationContext).requestUpdate()
            } catch (e: Exception) {
                Log.w(TAG, "Widget update request failed: ${e.message}")
            }

            // 11. Set neutral dark wallpaper for clean home screen / widget presentation
            try {
                val wallpaperManager = android.app.WallpaperManager.getInstance(context)
                val darkBitmap = android.graphics.Bitmap.createBitmap(1080, 2400, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(darkBitmap)
                canvas.drawColor(android.graphics.Color.parseColor("#121413"))
                wallpaperManager.setBitmap(darkBitmap)
                Log.i(TAG, "Neutral dark wallpaper set to #121413")
            } catch (e: Exception) {
                Log.w(TAG, "Wallpaper update failed: ${e.message}")
            }

            db.close()
            Log.i(TAG, "Demo data seeding complete.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seed demo data", e)
            return false
        }
    }
}

/**
 * BroadcastReceiver triggered via:
 * adb shell am broadcast -a com.kcalgrindai.app.action.SEED_DEMO_DATA
 */
class DemoDataSeederReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!BuildConfig.DEBUG) return
        if (intent?.action == DemoDataSeeder.ACTION_SEED_DEMO_DATA) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    DemoDataSeeder.seed(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
