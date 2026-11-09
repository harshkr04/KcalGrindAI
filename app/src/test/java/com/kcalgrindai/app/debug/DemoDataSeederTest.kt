package com.kcalgrindai.app.debug

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.seed.IndianFoodDataset
import com.kcalgrindai.app.data.repository.MealLogRepositoryImpl
import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class DemoDataSeederTest {

    @Test
    fun testDemoDataSeederSeedsAccurately() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Run seeder
        val success = DemoDataSeeder.seed(context)
        assertTrue("Seeder should execute successfully in debug", success)

        // 2. Open DB and verify profile and calculated targets
        val db = androidx.room.Room.databaseBuilder(
            context,
            KcalGrindDatabase::class.java,
            KcalGrindDatabase.DATABASE_NAME
        ).build()

        val profile = db.userProfileDao().getProfile()
        assertNotNull("Seeded profile should exist", profile)
        assertEquals("Alex", profile!!.firstName)
        assertEquals(28, profile.age)
        assertEquals(178.0, profile.heightCm, 0.01)
        assertEquals(78.0, profile.weightKg, 0.01)
        assertEquals("LOSE", profile.goal)
        assertEquals("MODERATE", profile.activityLevel)

        // Validate that calculated target matches 2,150 kcal deterministically
        val calcResult = NutritionCalculator.calculate(
            weightKg = profile.weightKg,
            heightCm = profile.heightCm,
            age = profile.age,
            goal = GoalType.LOSE,
            activity = ActivityLevel.MODERATE,
            goalWeightKg = profile.goalWeightKg,
            sex = "male"
        )

        // Explanation proof: BMR, TDEE, Deficit
        assertEquals(2150, calcResult.calories)
        assertEquals(1675.0, calcResult.bmr!!, 0.1) // 10*78 + 6.25*178 - 5*28 - 78 = 1674.5 -> 1675.0
        assertEquals(2595.0, calcResult.tdee!!, 0.1) // 1674.5 * 1.55 = 2595.475 -> 2595.0
        assertEquals(-450, calcResult.delta) // GoalType.LOSE default delta is -450 kcal

        // Goal saved in database matches calculate() output
        val goal = db.nutritionGoalDao().getLatestGoal()
        assertNotNull("Seeded goal should exist", goal)
        assertEquals(calcResult.calories, goal!!.calories)
        assertEquals(calcResult.proteinG, goal.proteinG, 0.1)
        assertEquals(calcResult.carbsG, goal.carbsG, 0.1)
        assertEquals(calcResult.fatG, goal.fatG, 0.1)
        assertEquals(calcResult.bmr, goal.bmr)
        assertEquals(calcResult.tdee, goal.tdee)

        // Macro sum matches ~2,150 kcal
        val macroCalorieSum = (goal.proteinG * 4 + goal.carbsG * 4 + goal.fatG * 9).toInt()
        assertEquals(2151, macroCalorieSum)
        assertTrue("Macro sum must be within 10 kcal of target", Math.abs(macroCalorieSum - 2150) <= 10)

        // 3. Prove existence of linked curated entries in the bundled database
        val dalIfct = db.foodDao().getFoodByExternalId(IndianFoodDataset.DATASET_SOURCE_RECIPE, "IFCT_D01")
        assertNotNull("IFCT_D01 must exist in bundled database", dalIfct)
        println("PROVEN CURATED ENTRY: ID=${dalIfct!!.id}, externalId=${dalIfct.externalId}, name='${dalIfct.name}', brand='${dalIfct.brand}'")
        assertEquals("Moong Dal Tadka (Yellow Moong)", dalIfct.name)
        assertEquals("Curated Indian Recipe Estimate", dalIfct.brand)

        val paneerIfct = db.foodDao().getFoodByExternalId(IndianFoodDataset.DATASET_SOURCE_RECIPE, "IFCT_P06")
        assertNotNull("IFCT_P06 must exist in bundled database", paneerIfct)
        println("PROVEN CURATED ENTRY: ID=${paneerIfct!!.id}, externalId=${paneerIfct.externalId}, name='${paneerIfct.name}', brand='${paneerIfct.brand}'")
        assertEquals("Paneer Bhurji", paneerIfct.name)
        assertEquals("Curated Indian Recipe Estimate", paneerIfct.brand)

        // 4. Verify Today's Meals: 1,623 kcal, 120P / 180C / 47F
        val today = LocalDate.now().toString()
        val meals = db.mealLogDao().getMealLogsByDate(today)
        assertEquals(4, meals.size)

        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0

        val mealItems = mutableListOf<com.kcalgrindai.app.data.local.entity.FoodLogItemEntity>()
        for (meal in meals) {
            totalCalories += meal.totalCalories
            val items = db.foodLogItemDao().getItemsForMeal(meal.id)
            for (item in items) {
                mealItems.add(item)
                totalProtein += item.proteinG
                totalCarbs += item.carbsG
                totalFat += item.fatG
            }
        }

        assertEquals(1623.0, totalCalories, 0.01)
        assertEquals(120.0, totalProtein, 0.01)
        assertEquals(180.0, totalCarbs, 0.01)
        assertEquals(47.0, totalFat, 0.01)

        val dalItem = mealItems.find { it.name == "Dal tadka & roti" }
        assertNotNull(dalItem)
        assertEquals(dalIfct.id, dalItem!!.foodId)
        assertEquals("curated_recipe", dalItem.source)

        val chickenItem = mealItems.find { it.name == "Chicken breast & rice" }
        assertNotNull(chickenItem)
        assertNull("Unbundled food must have null foodId", chickenItem!!.foodId)
        assertEquals("USER_ENTERED", chickenItem.source)
        assertEquals("Demo Custom Food", chickenItem.brand)

        val paneerItem = mealItems.find { it.name == "Paneer bhurji & paratha" }
        assertNotNull(paneerItem)
        assertEquals(paneerIfct.id, paneerItem!!.foodId)
        assertEquals("curated_recipe", paneerItem.source)

        val shakeItem = mealItems.find { it.name == "Whey protein shake with banana & peanut butter" }
        assertNotNull(shakeItem)
        assertNull("Unbundled food must have null foodId", shakeItem!!.foodId)
        assertEquals("USER_ENTERED", shakeItem.source)
        assertEquals("Demo Custom Food", shakeItem.brand)
        assertEquals(283.0, shakeItem.calories, 0.01)
        assertEquals(28.0, shakeItem.proteinG, 0.01)
        assertEquals(24.0, shakeItem.carbsG, 0.01)
        assertEquals(8.0, shakeItem.fatG, 0.01)

        // 5. Verify Today's Water: 1,250 ml
        val waterLogs = db.waterLogDao().getWaterLogsByDate(today)
        val totalWater = waterLogs.sumOf { it.amountMl }
        assertEquals(1250, totalWater)

        // 6. Verify 7-day streak
        val loggedDates = db.mealLogDao().getAllMealLogs().map { it.date }.distinct()
        val streak = MealLogRepositoryImpl.calculateStreak(loggedDates)
        assertEquals(7, streak)

        // 7. Verify Coach Conversation
        val conversations = db.conversationDao().getAllConversations()
        assertTrue("Conversation must exist", conversations.isNotEmpty())
        val messages = db.messageDao().getMessagesForConversation(conversations.first().id)
        assertEquals(2, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals("What should I eat for dinner with 40g protein?", messages[0].text)
        assertEquals("assistant", messages[1].role)
        assertTrue("Assistant reply must mention 40g protein", messages[1].text.contains("40g of protein") || messages[1].text.contains("40g protein"))
        assertTrue("Assistant reply must fit 527 kcal", messages[1].text.contains("527 calories") || messages[1].text.contains("527 kcal"))

        db.close()
    }
}
