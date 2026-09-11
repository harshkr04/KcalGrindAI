package com.lumina.nutrition.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.lumina.nutrition.data.local.entity.ConversationEntity
import com.lumina.nutrition.data.local.model.ConversationWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Upsert
    suspend fun upsertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Transaction
    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationWithMessages(id: Long): ConversationWithMessages?

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt ASC")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    fun observeAllConversations(): Flow<List<ConversationEntity>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun observeConversationWithMessages(id: Long): Flow<ConversationWithMessages?>

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC LIMIT 1")
    suspend fun getLatestConversation(): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC LIMIT 1")
    fun observeLatestConversation(): Flow<ConversationEntity?>

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)
}
