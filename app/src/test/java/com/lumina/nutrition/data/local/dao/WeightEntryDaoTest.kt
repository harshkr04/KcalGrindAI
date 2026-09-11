package com.lumina.nutrition.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.lumina.nutrition.data.local.LuminaDatabase
import com.lumina.nutrition.data.local.entity.WeightEntryEntity
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
class WeightEntryDaoTest {

    private lateinit var db: LuminaDatabase
    private lateinit var weightEntryDao: WeightEntryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LuminaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        weightEntryDao = db.weightEntryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndObserveLatestWeight() = runTest {
        val entry1 = WeightEntryEntity(
            id = 1L,
            weightKg = 75.0,
            date = "2026-08-30",
            note = "Morning weigh-in",
            loggedAt = 1000L
        )
        val entry2 = WeightEntryEntity(
            id = 2L,
            weightKg = 74.5,
            date = "2026-09-01",
            note = "Post workout",
            loggedAt = 2000L
        )

        weightEntryDao.insertWeightEntry(entry1)
        weightEntryDao.insertWeightEntry(entry2)

        weightEntryDao.observeLatestWeightEntry().test {
            val latest = awaitItem()
            assertNotNull(latest)
            assertEquals(74.5, latest!!.weightKg, 0.0)
            assertEquals("2026-09-01", latest.date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeWeightEntriesInRange() = runTest {
        val entry1 = WeightEntryEntity(
            id = 1L,
            weightKg = 75.0,
            date = "2026-08-25",
            note = null,
            loggedAt = 1000L
        )
        val entry2 = WeightEntryEntity(
            id = 2L,
            weightKg = 74.8,
            date = "2026-08-28",
            note = null,
            loggedAt = 2000L
        )
        val entry3 = WeightEntryEntity(
            id = 3L,
            weightKg = 74.2,
            date = "2026-09-02",
            note = null,
            loggedAt = 3000L
        )

        weightEntryDao.insertWeightEntry(entry1)
        weightEntryDao.insertWeightEntry(entry2)
        weightEntryDao.insertWeightEntry(entry3)

        weightEntryDao.observeWeightEntriesInRange("2026-08-26", "2026-09-01").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(74.8, list[0].weightKg, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteWeightEntry() = runTest {
        val entry = WeightEntryEntity(
            id = 1L,
            weightKg = 70.0,
            date = "2026-09-01",
            note = null,
            loggedAt = 1000L
        )
        weightEntryDao.insertWeightEntry(entry)
        weightEntryDao.deleteWeightEntryById(1L)

        val retrieved = weightEntryDao.getWeightEntryById(1L)
        assertNull(retrieved)
    }
}
