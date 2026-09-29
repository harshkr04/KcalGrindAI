package com.kcalgrindai.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.ConversationEntity
import com.kcalgrindai.app.data.local.entity.MessageEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConversationAndMessageDaoTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var messageDao: MessageDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        conversationDao = db.conversationDao()
        messageDao = db.messageDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertConversationAndMessagesThenObserve() = runTest {
        val conv = ConversationEntity(
            id = 1L,
            startedAt = 1000L,
            lastMessageAt = 1000L
        )
        conversationDao.insertConversation(conv)

        val m1 = MessageEntity(
            id = 1L,
            conversationId = 1L,
            role = "user",
            text = "How much protein should I have after a workout?",
            structuredDataJson = null,
            createdAt = 1000L
        )
        val m2 = MessageEntity(
            id = 2L,
            conversationId = 1L,
            role = "assistant",
            text = "Aim for 20-30g of high quality protein within 2 hours of your workout.",
            structuredDataJson = """{"recommendedProteinG": 25}""",
            createdAt = 1050L
        )
        messageDao.insertMessages(listOf(m1, m2))

        conversationDao.observeConversationWithMessages(1L).test {
            val convWithMessages = awaitItem()
            assertNotNull(convWithMessages)
            assertEquals(2, convWithMessages!!.messages.size)
            assertEquals("user", convWithMessages.messages[0].role)
            assertEquals("assistant", convWithMessages.messages[1].role)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cascadeDeleteConversationDeletesMessages() = runTest {
        val conv = ConversationEntity(
            id = 2L,
            startedAt = 2000L,
            lastMessageAt = 2000L
        )
        conversationDao.insertConversation(conv)

        val msg = MessageEntity(
            id = 10L,
            conversationId = 2L,
            role = "user",
            text = "Hello",
            structuredDataJson = null,
            createdAt = 2000L
        )
        messageDao.insertMessage(msg)

        conversationDao.deleteConversationById(2L)

        val convLoaded = conversationDao.getConversationById(2L)
        assertNull(convLoaded)

        val messagesLoaded = messageDao.getMessagesForConversation(2L)
        assertEquals(0, messagesLoaded.size)
    }
}
