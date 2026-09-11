package com.lumina.nutrition.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.lumina.nutrition.data.local.LuminaDatabase
import com.lumina.nutrition.data.local.entity.AIAnalysisEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AIAnalysisDaoTest {

    private lateinit var db: LuminaDatabase
    private lateinit var aiAnalysisDao: AIAnalysisDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LuminaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        aiAnalysisDao = db.aiAnalysisDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndObserveRecentAnalyses() = runTest {
        val a1 = AIAnalysisEntity(
            id = 1L,
            inputType = "photo",
            rawInputRef = "file:///images/meal1.jpg",
            resultJson = """{"foods": ["Apple", "Oatmeal"]}""",
            overallConfidence = 0.92f,
            createdAt = 1000L
        )
        val a2 = AIAnalysisEntity(
            id = 2L,
            inputType = "voice",
            rawInputRef = "two eggs and toast",
            resultJson = """{"foods": ["Eggs", "Toast"]}""",
            overallConfidence = 0.88f,
            createdAt = 2000L
        )

        aiAnalysisDao.insertAnalysis(a1)
        aiAnalysisDao.insertAnalysis(a2)

        val retrieved = aiAnalysisDao.getAnalysisById(1L)
        assertNotNull(retrieved)
        assertEquals("photo", retrieved!!.inputType)

        aiAnalysisDao.observeRecentAnalyses(10).test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals(2L, list[0].id) // ordered by createdAt DESC
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteOlderThan() = runTest {
        val oldRecord = AIAnalysisEntity(
            id = 1L,
            inputType = "text",
            rawInputRef = "coffee with milk",
            resultJson = "{}",
            overallConfidence = 0.85f,
            createdAt = 1000L
        )
        val newRecord = AIAnalysisEntity(
            id = 2L,
            inputType = "photo",
            rawInputRef = "file:///image.jpg",
            resultJson = "{}",
            overallConfidence = 0.95f,
            createdAt = 5000L
        )

        aiAnalysisDao.insertAnalysis(oldRecord)
        aiAnalysisDao.insertAnalysis(newRecord)

        val deletedCount = aiAnalysisDao.deleteOlderThan(3000L)
        assertEquals(1, deletedCount)

        val remaining = aiAnalysisDao.getAnalysisById(1L)
        assertEquals(null, remaining)
        assertNotNull(aiAnalysisDao.getAnalysisById(2L))
    }
}
