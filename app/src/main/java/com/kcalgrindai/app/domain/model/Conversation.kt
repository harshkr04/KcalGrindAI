package com.kcalgrindai.app.domain.model

data class Conversation(
    val id: Long = 0L,
    val startedAt: Long,
    val lastMessageAt: Long,
    val messages: List<ChatMessage> = emptyList()
)
