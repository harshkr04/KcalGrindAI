package com.lumina.nutrition.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.lumina.nutrition.data.local.entity.ConversationEntity
import com.lumina.nutrition.data.local.entity.MessageEntity

data class ConversationWithMessages(
    @Embedded
    val conversation: ConversationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "conversationId"
    )
    val messages: List<MessageEntity>
)
