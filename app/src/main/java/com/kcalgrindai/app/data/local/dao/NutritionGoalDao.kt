package com.kcalgrindai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: NutritionGoalEntity): Long

    @Upsert
    suspend fun upsertGoal(goal: NutritionGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: NutritionGoalEntity)

    @Query("SELECT * FROM nutrition_goals WHERE userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getGoalByUserId(userId: Long): NutritionGoalEntity?

    @Query("SELECT * FROM nutrition_goals ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestGoal(): NutritionGoalEntity?

    @Query("SELECT * FROM nutrition_goals WHERE userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    fun observeGoalByUserId(userId: Long): Flow<NutritionGoalEntity?>

    @Query("SELECT * FROM nutrition_goals ORDER BY updatedAt DESC LIMIT 1")
    fun observeLatestGoal(): Flow<NutritionGoalEntity?>

    @Query("DELETE FROM nutrition_goals WHERE userId = :userId")
    suspend fun deleteGoalByUserId(userId: Long)

    @Delete
    suspend fun deleteGoal(goal: NutritionGoalEntity)
}
