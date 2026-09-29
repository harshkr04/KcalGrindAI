package com.kcalgrindai.app.data.di

import android.content.Context
import androidx.room.Room
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.dao.AIAnalysisDao
import com.kcalgrindai.app.data.local.dao.ConversationDao
import com.kcalgrindai.app.data.local.dao.FoodDao
import com.kcalgrindai.app.data.local.dao.FoodLogItemDao
import com.kcalgrindai.app.data.local.dao.MealLogDao
import com.kcalgrindai.app.data.local.dao.MessageDao
import com.kcalgrindai.app.data.local.dao.NutritionGoalDao
import com.kcalgrindai.app.data.local.dao.UserProfileDao
import com.kcalgrindai.app.data.local.dao.WeightEntryDao
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
    fun provideKcalGrindDatabase(
        @ApplicationContext context: Context
    ): KcalGrindDatabase {
        return Room.databaseBuilder(
            context,
            KcalGrindDatabase::class.java,
            KcalGrindDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserProfileDao(database: KcalGrindDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideNutritionGoalDao(database: KcalGrindDatabase): NutritionGoalDao {
        return database.nutritionGoalDao()
    }

    @Provides
    fun provideFoodDao(database: KcalGrindDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideMealLogDao(database: KcalGrindDatabase): MealLogDao {
        return database.mealLogDao()
    }

    @Provides
    fun provideFoodLogItemDao(database: KcalGrindDatabase): FoodLogItemDao {
        return database.foodLogItemDao()
    }

    @Provides
    fun provideWeightEntryDao(database: KcalGrindDatabase): WeightEntryDao {
        return database.weightEntryDao()
    }

    @Provides
    fun provideAIAnalysisDao(database: KcalGrindDatabase): AIAnalysisDao {
        return database.aiAnalysisDao()
    }

    @Provides
    fun provideConversationDao(database: KcalGrindDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideMessageDao(database: KcalGrindDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideWaterLogDao(database: KcalGrindDatabase): com.kcalgrindai.app.data.local.dao.WaterLogDao {
        return database.waterLogDao()
    }

    @Provides
    fun provideRecipeDao(database: KcalGrindDatabase): com.kcalgrindai.app.data.local.dao.RecipeDao {
        return database.recipeDao()
    }
}
