package com.lumina.nutrition.data.repository

import com.lumina.nutrition.data.local.dao.ConversationDao
import com.lumina.nutrition.data.local.dao.MessageDao
import com.lumina.nutrition.data.local.entity.ConversationEntity
import com.lumina.nutrition.data.mapper.toDomain
import com.lumina.nutrition.data.mapper.toEntity
import com.lumina.nutrition.data.remote.api.LuminaAiApiService
import com.lumina.nutrition.data.remote.dto.ChatMessageDto
import com.lumina.nutrition.data.remote.dto.ChatRequestDto
import com.lumina.nutrition.data.remote.dto.UserContextDto
import com.lumina.nutrition.domain.model.AIChatResponse
import com.lumina.nutrition.domain.model.ChatMessage
import com.lumina.nutrition.domain.model.Conversation
import com.lumina.nutrition.domain.model.MessageRole
import com.lumina.nutrition.domain.repository.AICoachRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AICoachRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val aiApiService: LuminaAiApiService
) : AICoachRepository {

    override suspend fun createConversation(startedAt: Long): Long {
        val entity = ConversationEntity(startedAt = startedAt, lastMessageAt = startedAt)
        return conversationDao.insertConversation(entity)
    }

    override suspend fun updateConversationLastMessage(
        conversationId: Long,
        lastMessageAt: Long
    ) {
        val existing = conversationDao.getConversationById(conversationId)
        if (existing != null) {
            conversationDao.updateConversation(existing.copy(lastMessageAt = lastMessageAt))
        }
    }

    override suspend fun getConversationById(id: Long): Conversation? {
        return conversationDao.getConversationWithMessages(id)?.toDomain()
    }

    override fun observeAllConversations(): Flow<List<Conversation>> {
        return conversationDao.observeAllConversations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeConversationWithMessages(id: Long): Flow<Conversation?> {
        return conversationDao.observeConversationWithMessages(id).map { it?.toDomain() }
    }

    override fun observeLatestConversation(): Flow<Conversation?> {
        return conversationDao.observeLatestConversation().map { entity ->
            entity?.let {
                val messages = messageDao.getMessagesForConversation(it.id).map { m -> m.toDomain() }
                it.toDomain(messages)
            }
        }
    }

    override suspend fun getLatestConversation(): Conversation? {
        val entity = conversationDao.getLatestConversation() ?: return null
        val messages = messageDao.getMessagesForConversation(entity.id).map { it.toDomain() }
        return entity.toDomain(messages)
    }

    override suspend fun deleteConversation(id: Long) {
        conversationDao.deleteConversationById(id)
    }

    override suspend fun saveMessage(message: ChatMessage): Long {
        val messageId = messageDao.insertMessage(message.toEntity())
        updateConversationLastMessage(message.conversationId, message.createdAt)
        return messageId
    }

    override suspend fun getMessagesForConversation(conversationId: Long): List<ChatMessage> {
        return messageDao.getMessagesForConversation(conversationId).map { it.toDomain() }
    }

    override fun observeMessagesForConversation(conversationId: Long): Flow<List<ChatMessage>> {
        return messageDao.observeMessagesForConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun sendChatMessage(
        conversationId: Long,
        userContent: String,
        remainingCalories: Int?,
        consumedCalories: Int?,
        targetCalories: Int?,
        goal: String?,
        recentTrends: com.lumina.nutrition.data.remote.dto.RecentTrendsDto?
    ): Result<AIChatResponse> = runCatching {
        val now = System.currentTimeMillis()
        // 1. Save user message locally
        saveMessage(
            ChatMessage(
                id = 0L,
                conversationId = conversationId,
                role = MessageRole.USER,
                text = userContent,
                structuredDataJson = null,
                createdAt = now
            )
        )

        // 2. Fetch history for context
        val messages = messageDao.getMessagesForConversation(conversationId)
        val chatDtos = messages.map {
            ChatMessageDto(role = it.role, content = it.text)
        }

        // 3. Call backend proxy
        val responseDto = aiApiService.chat(
            ChatRequestDto(
                messages = chatDtos,
                userContext = UserContextDto(
                    remainingCalories = remainingCalories,
                    consumedCalories = consumedCalories,
                    targetCalories = targetCalories,
                    goal = goal,
                    recentTrends = recentTrends
                )
            )
        )

        val replyText = responseDto.reply ?: "I'm here to support your nutrition journey. How else can I help?"

        // 4. Save assistant reply to Room (including suggestedAction JSON if any)
        saveMessage(
            ChatMessage(
                id = 0L,
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                text = replyText,
                structuredDataJson = responseDto.suggestedAction,
                createdAt = System.currentTimeMillis()
            )
        )

        AIChatResponse(
            reply = replyText,
            suggestedAction = responseDto.suggestedAction
        )
    }
}
