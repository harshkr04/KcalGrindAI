package com.lumina.nutrition.data.di

import android.content.Context
import androidx.room.Room
import com.lumina.nutrition.data.local.LuminaDatabase
import com.lumina.nutrition.data.local.dao.AIAnalysisDao
import com.lumina.nutrition.data.local.dao.ConversationDao
import com.lumina.nutrition.data.local.dao.FoodDao
import com.lumina.nutrition.data.local.dao.FoodLogItemDao
import com.lumina.nutrition.data.local.dao.MealLogDao
import com.lumina.nutrition.data.local.dao.MessageDao
import com.lumina.nutrition.data.local.dao.NutritionGoalDao
import com.lumina.nutrition.data.local.dao.UserProfileDao
import com.lumina.nutrition.data.local.dao.WeightEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLuminaDatabase(
        @ApplicationContext context: Context
    ): LuminaDatabase {
        return Room.databaseBuilder(
            context,
            LuminaDatabase::class.java,
            LuminaDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserProfileDao(database: LuminaDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideNutritionGoalDao(database: LuminaDatabase): NutritionGoalDao {
        return database.nutritionGoalDao()
    }

    @Provides
    fun provideFoodDao(database: LuminaDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideMealLogDao(database: LuminaDatabase): MealLogDao {
        return database.mealLogDao()
    }

    @Provides
    fun provideFoodLogItemDao(database: LuminaDatabase): FoodLogItemDao {
        return database.foodLogItemDao()
    }

    @Provides
    fun provideWeightEntryDao(database: LuminaDatabase): WeightEntryDao {
        return database.weightEntryDao()
    }

    @Provides
    fun provideAIAnalysisDao(database: LuminaDatabase): AIAnalysisDao {
        return database.aiAnalysisDao()
    }

    @Provides
    fun provideConversationDao(database: LuminaDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideMessageDao(database: LuminaDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideWaterLogDao(database: LuminaDatabase): com.lumina.nutrition.data.local.dao.WaterLogDao {
        return database.waterLogDao()
    }
}
