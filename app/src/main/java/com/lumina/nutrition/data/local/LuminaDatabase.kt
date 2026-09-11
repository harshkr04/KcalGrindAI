package com.lumina.nutrition.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lumina.nutrition.data.local.converter.Converters
import com.lumina.nutrition.data.local.dao.AIAnalysisDao
import com.lumina.nutrition.data.local.dao.ConversationDao
import com.lumina.nutrition.data.local.dao.FoodDao
import com.lumina.nutrition.data.local.dao.FoodLogItemDao
import com.lumina.nutrition.data.local.dao.MealLogDao
import com.lumina.nutrition.data.local.dao.MessageDao
import com.lumina.nutrition.data.local.dao.NutritionGoalDao
import com.lumina.nutrition.data.local.dao.UserProfileDao
import com.lumina.nutrition.data.local.dao.WaterLogDao
import com.lumina.nutrition.data.local.dao.WeightEntryDao
import com.lumina.nutrition.data.local.entity.AIAnalysisEntity
import com.lumina.nutrition.data.local.entity.ConversationEntity
import com.lumina.nutrition.data.local.entity.FoodEntity
import com.lumina.nutrition.data.local.entity.FoodLogItemEntity
import com.lumina.nutrition.data.local.entity.MealLogEntity
import com.lumina.nutrition.data.local.entity.MessageEntity
import com.lumina.nutrition.data.local.entity.NutritionGoalEntity
import com.lumina.nutrition.data.local.entity.UserProfileEntity
import com.lumina.nutrition.data.local.entity.WaterLogEntity
import com.lumina.nutrition.data.local.entity.WeightEntryEntity

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
        WaterLogEntity::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LuminaDatabase : RoomDatabase() {
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

    companion object {
        const val DATABASE_NAME = "lumina_nutrition.db"
    }
}
