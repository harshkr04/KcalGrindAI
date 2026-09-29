package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.AIChatResponse
import com.kcalgrindai.app.domain.model.ChatMessage
import com.kcalgrindai.app.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface AICoachRepository {
    suspend fun createConversation(startedAt: Long = System.currentTimeMillis()): Long
    suspend fun updateConversationLastMessage(conversationId: Long, lastMessageAt: Long)
    suspend fun getConversationById(id: Long): Conversation?
    fun observeAllConversations(): Flow<List<Conversation>>
    fun observeConversationWithMessages(id: Long): Flow<Conversation?>
    fun observeLatestConversation(): Flow<Conversation?>
    suspend fun getLatestConversation(): Conversation?
    suspend fun deleteConversation(id: Long)
    suspend fun saveMessage(message: ChatMessage): Long
    suspend fun getMessagesForConversation(conversationId: Long): List<ChatMessage>
    fun observeMessagesForConversation(conversationId: Long): Flow<List<ChatMessage>>

    suspend fun sendChatMessage(
        conversationId: Long,
        userContent: String,
        remainingCalories: Int? = null,
        consumedCalories: Int? = null,
        targetCalories: Int? = null,
        goal: String? = null,
        recentTrends: com.kcalgrindai.app.data.remote.dto.RecentTrendsDto? = null
    ): Result<AIChatResponse>
}

