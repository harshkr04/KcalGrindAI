package com.kcalgrindai.app.data.di

import com.kcalgrindai.app.data.repository.AIAnalysisRepositoryImpl
import com.kcalgrindai.app.data.repository.AICoachRepositoryImpl
import com.kcalgrindai.app.data.repository.FoodRepositoryImpl
import com.kcalgrindai.app.data.repository.MealLogRepositoryImpl
import com.kcalgrindai.app.data.repository.NutritionGoalRepositoryImpl
import com.kcalgrindai.app.data.repository.SyncRepositoryImpl
import com.kcalgrindai.app.data.repository.UserProfileRepositoryImpl
import com.kcalgrindai.app.data.repository.WeightRepositoryImpl
import com.kcalgrindai.app.domain.repository.AIAnalysisRepository
import com.kcalgrindai.app.domain.repository.AICoachRepository
import com.kcalgrindai.app.domain.repository.FoodRepository
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.SyncRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.domain.repository.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindNutritionGoalRepository(
        impl: NutritionGoalRepositoryImpl
    ): NutritionGoalRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        impl: FoodRepositoryImpl
    ): FoodRepository

    @Binds
    @Singleton
    abstract fun bindMealLogRepository(
        impl: MealLogRepositoryImpl
    ): MealLogRepository

    @Binds
    @Singleton
    abstract fun bindWeightRepository(
        impl: WeightRepositoryImpl
    ): WeightRepository

    @Binds
    @Singleton
    abstract fun bindAIAnalysisRepository(
        impl: AIAnalysisRepositoryImpl
    ): AIAnalysisRepository

    @Binds
    @Singleton
    abstract fun bindAICoachRepository(
        impl: AICoachRepositoryImpl
    ): AICoachRepository

    @Binds
    @Singleton
    abstract fun bindAICoachUsageRepository(
        impl: com.kcalgrindai.app.data.repository.AICoachUsageRepositoryImpl
    ): com.kcalgrindai.app.domain.repository.AICoachUsageRepository

    @Binds
    @Singleton
    abstract fun bindWaterLogRepository(
        impl: com.kcalgrindai.app.data.repository.WaterLogRepositoryImpl
    ): com.kcalgrindai.app.domain.repository.WaterLogRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        impl: com.kcalgrindai.app.data.repository.RecipeRepositoryImpl
    ): com.kcalgrindai.app.domain.repository.RecipeRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: com.kcalgrindai.app.data.repository.UserPreferencesRepositoryImpl
    ): com.kcalgrindai.app.domain.repository.UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        impl: com.kcalgrindai.app.data.repository.BillingRepositoryImpl
    ): com.kcalgrindai.app.domain.repository.BillingRepository
}
