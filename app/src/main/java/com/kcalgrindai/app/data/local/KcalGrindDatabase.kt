package com.kcalgrindai.app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kcalgrindai.app.data.local.converter.Converters
import com.kcalgrindai.app.data.local.dao.AIAnalysisDao
import com.kcalgrindai.app.data.local.dao.ConversationDao
import com.kcalgrindai.app.data.local.dao.FoodDao
import com.kcalgrindai.app.data.local.dao.FoodLogItemDao
import com.kcalgrindai.app.data.local.dao.MealLogDao
import com.kcalgrindai.app.data.local.dao.MessageDao
import com.kcalgrindai.app.data.local.dao.NutritionGoalDao
import com.kcalgrindai.app.data.local.dao.RecipeDao
import com.kcalgrindai.app.data.local.dao.UserProfileDao
import com.kcalgrindai.app.data.local.dao.WaterLogDao
import com.kcalgrindai.app.data.local.dao.WeightEntryDao
import com.kcalgrindai.app.data.local.entity.AIAnalysisEntity
import com.kcalgrindai.app.data.local.entity.ConversationEntity
import com.kcalgrindai.app.data.local.entity.FoodEntity
import com.kcalgrindai.app.data.local.entity.FoodLogItemEntity
import com.kcalgrindai.app.data.local.entity.MealLogEntity
import com.kcalgrindai.app.data.local.entity.MessageEntity
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import com.kcalgrindai.app.data.local.entity.RecipeEntity
import com.kcalgrindai.app.data.local.entity.UserProfileEntity
import com.kcalgrindai.app.data.local.entity.WaterLogEntity
import com.kcalgrindai.app.data.local.entity.WeightEntryEntity

@Database(
    entities = [
        UserProfileEntity::class,
        NutritionGoalEntity::class,
        FoodEntity::class,
        MealLogEntity::class,
        FoodLogItemEntity::class,
        WeightEntryEntity::class,
        AIAnalysisEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        WaterLogEntity::class,
        RecipeEntity::class
    ],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KcalGrindDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun nutritionGoalDao(): NutritionGoalDao
    abstract fun foodDao(): FoodDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun foodLogItemDao(): FoodLogItemDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun aiAnalysisDao(): AIAnalysisDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        const val DATABASE_NAME = "kcalgrindai.db"
    }
}
