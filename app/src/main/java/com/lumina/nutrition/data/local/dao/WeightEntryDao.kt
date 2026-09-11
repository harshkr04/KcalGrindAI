package com.lumina.nutrition.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.lumina.nutrition.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntryEntity): Long

    @Upsert
    suspend fun upsertWeightEntry(entry: WeightEntryEntity): Long

    @Update
    suspend fun updateWeightEntry(entry: WeightEntryEntity)

    @Query("SELECT * FROM weight_entries WHERE id = :id LIMIT 1")
    suspend fun getWeightEntryById(id: Long): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries ORDER BY date DESC, loggedAt DESC LIMIT 1")
    suspend fun getLatestWeightEntry(): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries ORDER BY date DESC, loggedAt DESC LIMIT 1")
    fun observeLatestWeightEntry(): Flow<WeightEntryEntity?>

    @Query("SELECT * FROM weight_entries ORDER BY date ASC, loggedAt ASC")
    suspend fun getAllWeightEntries(): List<WeightEntryEntity>

    @Query("SELECT * FROM weight_entries ORDER BY date ASC, loggedAt ASC")
    fun observeAllWeightEntries(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, loggedAt ASC")
    fun observeWeightEntriesInRange(startDate: String, endDate: String): Flow<List<WeightEntryEntity>>

    @Delete
    suspend fun deleteWeightEntry(entry: WeightEntryEntity)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteWeightEntryById(id: Long)
}
