package com.lumina.nutrition.data.di

import com.lumina.nutrition.data.repository.AIAnalysisRepositoryImpl
import com.lumina.nutrition.data.repository.AICoachRepositoryImpl
import com.lumina.nutrition.data.repository.FoodRepositoryImpl
import com.lumina.nutrition.data.repository.MealLogRepositoryImpl
import com.lumina.nutrition.data.repository.NutritionGoalRepositoryImpl
import com.lumina.nutrition.data.repository.SyncRepositoryImpl
import com.lumina.nutrition.data.repository.UserProfileRepositoryImpl
import com.lumina.nutrition.data.repository.WeightRepositoryImpl
import com.lumina.nutrition.domain.repository.AIAnalysisRepository
import com.lumina.nutrition.domain.repository.AICoachRepository
import com.lumina.nutrition.domain.repository.FoodRepository
import com.lumina.nutrition.domain.repository.MealLogRepository
import com.lumina.nutrition.domain.repository.NutritionGoalRepository
import com.lumina.nutrition.domain.repository.SyncRepository
import com.lumina.nutrition.domain.repository.UserProfileRepository
import com.lumina.nutrition.domain.repository.WeightRepository
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
    abstract fun bindWaterLogRepository(
        impl: com.lumina.nutrition.data.repository.WaterLogRepositoryImpl
    ): com.lumina.nutrition.domain.repository.WaterLogRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository
}
