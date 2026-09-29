package com.kcalgrindai.app.domain.model

data class ChatMessage(
    val id: Long = 0L,
    val conversationId: Long,
    val role: MessageRole,
    val text: String,
    val structuredDataJson: String? = null,
    val createdAt: Long
)
