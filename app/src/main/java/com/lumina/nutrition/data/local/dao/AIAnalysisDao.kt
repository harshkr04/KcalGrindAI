package com.lumina.nutrition.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumina.nutrition.data.local.entity.AIAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIAnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AIAnalysisEntity): Long

    @Query("SELECT * FROM ai_analyses WHERE id = :id LIMIT 1")
    suspend fun getAnalysisById(id: Long): AIAnalysisEntity?

    @Query("SELECT * FROM ai_analyses ORDER BY createdAt ASC")
    suspend fun getAllAnalyses(): List<AIAnalysisEntity>

    @Query("SELECT * FROM ai_analyses ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentAnalyses(limit: Int = 20): Flow<List<AIAnalysisEntity>>

    @Query("DELETE FROM ai_analyses WHERE createdAt < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM ai_analyses")
    suspend fun deleteAll()
}
