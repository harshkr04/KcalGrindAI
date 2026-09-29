package com.kcalgrindai.app.data.repository

import com.kcalgrindai.app.data.local.dao.ConversationDao
import com.kcalgrindai.app.data.local.dao.MessageDao
import com.kcalgrindai.app.data.local.entity.ConversationEntity
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.data.mapper.toEntity
import com.kcalgrindai.app.data.remote.api.KcalGrindAIAiApiService
import com.kcalgrindai.app.data.remote.dto.ChatMessageDto
import com.kcalgrindai.app.data.remote.dto.ChatRequestDto
import com.kcalgrindai.app.data.remote.dto.UserContextDto
import com.kcalgrindai.app.domain.model.AIChatResponse
import com.kcalgrindai.app.domain.model.ChatMessage
import com.kcalgrindai.app.domain.model.Conversation
import com.kcalgrindai.app.domain.model.AICoachQuotaExceededException
import com.kcalgrindai.app.domain.repository.AICoachRepository
import com.kcalgrindai.app.domain.repository.AICoachUsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AICoachRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val aiApiService: KcalGrindAIAiApiService,
    private val aiCoachUsageRepository: AICoachUsageRepository
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
        recentTrends: com.kcalgrindai.app.data.remote.dto.RecentTrendsDto?
    ): Result<AIChatResponse> = runCatching {
        // 1. Enforce quota limit at the repository / API request layer
        aiCoachUsageRepository.checkQuota()

        // 2. Fetch history for context
        val messages = messageDao.getMessagesForConversation(conversationId)
        val chatDtos = messages.map {
            ChatMessageDto(role = it.role, content = it.text)
        } + ChatMessageDto(role = "user", content = userContent)

        // 3. Call backend proxy
        val responseDto = try {
            aiApiService.chat(
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
        } catch (httpEx: retrofit2.HttpException) {
            if (httpEx.code() == 429) {
                val usage = aiCoachUsageRepository.getUsage()
                val limitMsg = if (usage.isPro) {
                    "You've reached today's Pro AI Coach limit."
                } else {
                    "You've reached today's free AI Coach limit. Your messages reset tomorrow."
                }
                throw AICoachQuotaExceededException(limitMsg)
            }
            throw httpEx
        }

        val replyText = responseDto.reply ?: "I'm here to support your nutrition journey. How else can I help?"
        val now = System.currentTimeMillis()

        // 4. On successful API response: Save messages to Room and consume exactly 1 message quota
        saveMessage(
            ChatMessage(
                id = 0L,
                conversationId = conversationId,
                role = com.kcalgrindai.app.domain.model.MessageRole.USER,
                text = userContent,
                structuredDataJson = null,
                createdAt = now
            )
        )

        saveMessage(
            ChatMessage(
                id = 0L,
                conversationId = conversationId,
                role = com.kcalgrindai.app.domain.model.MessageRole.ASSISTANT,
                text = replyText,
                structuredDataJson = responseDto.suggestedAction,
                createdAt = System.currentTimeMillis()
            )
        )

        // Consume quota ONLY on success
        aiCoachUsageRepository.recordMessageSent()

        AIChatResponse(
            reply = replyText,
            suggestedAction = responseDto.suggestedAction
        )
    }
}
