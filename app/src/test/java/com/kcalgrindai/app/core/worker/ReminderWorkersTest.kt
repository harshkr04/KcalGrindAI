package com.kcalgrindai.app.core.worker

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.kcalgrindai.app.core.notification.NotificationHelper
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.MealLogEntity
import com.kcalgrindai.app.data.local.entity.WaterLogEntity
import com.kcalgrindai.app.domain.model.ThemeMode
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class ReminderWorkersTest {

    private lateinit var context: Context
    private lateinit var database: KcalGrindDatabase
    private lateinit var fakePrefs: TestUserPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fakePrefs = TestUserPreferencesRepository()
        NotificationHelper.createNotificationChannels(context)
        org.robolectric.Shadows.shadowOf(context as android.app.Application)
            .grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private class TestUserPreferencesRepository(
        var notificationsOn: Boolean = true,
        var mealRemindersOn: Boolean = true,
        var hydrationRemindersOn: Boolean = true
    ) : UserPreferencesRepository {
        private val _notifications = MutableStateFlow(notificationsOn)
        private val _meal = MutableStateFlow(mealRemindersOn)
        private val _hydration = MutableStateFlow(hydrationRemindersOn)

        override val notificationsEnabled: Flow<Boolean> = _notifications.asStateFlow()
        override val darkModeEnabled: Flow<Boolean> = MutableStateFlow(false).asStateFlow()
        override val themeMode: Flow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM).asStateFlow()
        override val selectedAvatar: Flow<String?> = MutableStateFlow(null).asStateFlow()
        override val widgetPrivacyEnabled: Flow<Boolean> = MutableStateFlow(false).asStateFlow()
        override val widgetPromoDismissed: Flow<Boolean> = MutableStateFlow(false).asStateFlow()
        override val mealRemindersEnabled: Flow<Boolean> = _meal.asStateFlow()
        override val hydrationRemindersEnabled: Flow<Boolean> = _hydration.asStateFlow()
        override val deductActiveCaloriesEnabled: Flow<Boolean> = MutableStateFlow(false).asStateFlow()

        override suspend fun setNotificationsEnabled(enabled: Boolean) { _notifications.value = enabled }
        override suspend fun setDarkModeEnabled(enabled: Boolean) {}
        override suspend fun setThemeMode(mode: ThemeMode) {}
        override suspend fun setSelectedAvatar(avatar: String?) {}
        override suspend fun setWidgetPrivacyEnabled(enabled: Boolean) {}
        override suspend fun setWidgetPromoDismissed(dismissed: Boolean) {}
        override suspend fun setMealRemindersEnabled(enabled: Boolean) { _meal.value = enabled }
        private val lastMealReminderDates = mutableMapOf<String, String>()
        override suspend fun getLastMealReminderDate(mealType: String): String? = lastMealReminderDates[mealType.lowercase()]
        override suspend fun setLastMealReminderDate(mealType: String, date: String) {
            lastMealReminderDates[mealType.lowercase()] = date
        }
        override suspend fun setHydrationRemindersEnabled(enabled: Boolean) { _hydration.value = enabled }
        override suspend fun setDeductActiveCaloriesEnabled(enabled: Boolean) {}
        override suspend fun clearAllPreferences() { lastMealReminderDates.clear() }
    }

    @Test
    fun notificationChannels_createdSuccessfully() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertNotNull(notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID_MEALS))
        assertNotNull(notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID_HYDRATION))
    }

    @Test
    fun hydrationReminder_skipsIfRecentlyLogged() = runBlocking {
        val now = System.currentTimeMillis()
        val todayStr = LocalDate.now().toString()

        // Insert water logged 30 minutes ago
        database.waterLogDao().insertWaterLog(
            WaterLogEntity(
                id = 1L,
                amountMl = 250,
                loggedAt = now - (30 * 60 * 1000L),
                date = todayStr,
                )
        )

        var notificationSent = false
        val worker = TestListenableWorkerBuilder<HydrationReminderWorker>(context).build()
        worker.overrideWaterLogDao = database.waterLogDao()
        worker.overrideUserPreferencesRepository = fakePrefs
        worker.overrideCurrentTime = LocalTime.of(10, 0) // 10:00 AM (waking hours)
        worker.overrideCurrentMillis = now
        worker.overrideNotificationSender = { notificationSent = true }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(notificationSent) // Skipped because water was logged within 2 hours
    }

    @Test
    fun hydrationReminder_firesWhenNotRecentlyLoggedDuringWakingHours() = runBlocking {
        val now = System.currentTimeMillis()
        val todayStr = LocalDate.now().toString()

        // Insert water logged 4 hours ago (outside 2hr window)
        database.waterLogDao().insertWaterLog(
            WaterLogEntity(
                id = 1L,
                amountMl = 250,
                loggedAt = now - (4 * 60 * 60 * 1000L),
                date = todayStr,
                )
        )

        var notificationSent = false
        val worker = TestListenableWorkerBuilder<HydrationReminderWorker>(context).build()
        worker.overrideWaterLogDao = database.waterLogDao()
        worker.overrideUserPreferencesRepository = fakePrefs
        worker.overrideCurrentTime = LocalTime.of(14, 0) // 2:00 PM (waking hours)
        worker.overrideCurrentMillis = now
        worker.overrideNotificationSender = { notificationSent = true }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(notificationSent) // Fired because not logged within last 2 hours
    }

    @Test
    fun hydrationReminder_skipsDuringNightHours() = runBlocking {
        val now = System.currentTimeMillis()
        var notificationSent = false
        val worker = TestListenableWorkerBuilder<HydrationReminderWorker>(context).build()
        worker.overrideWaterLogDao = database.waterLogDao()
        worker.overrideUserPreferencesRepository = fakePrefs
        worker.overrideCurrentTime = LocalTime.of(3, 30) // 3:30 AM (sleeping hours)
        worker.overrideCurrentMillis = now
        worker.overrideNotificationSender = { notificationSent = true }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(notificationSent) // Skipped at night
    }

    @Test
    fun mealReminder_skipsIfMealAlreadyLogged() = runBlocking {
        val todayStr = LocalDate.now().toString()

        // Insert breakfast already logged
        database.mealLogDao().insertMealLog(
            MealLogEntity(
                id = 1L,
                date = todayStr,
                mealType = "BREAKFAST",
                totalCalories = 450.0,
                loggedAt = System.currentTimeMillis(),
                source = "USER_ENTERED",
                synced = true
            )
        )

        var notificationSent = false
        var triggeredMeal = ""
        val worker = TestListenableWorkerBuilder<MealReminderWorker>(context).build()
        worker.overrideMealLogDao = database.mealLogDao()
        worker.overrideUserPreferencesRepository = fakePrefs
        worker.overrideCurrentTime = LocalTime.of(8, 30) // 8:30 AM (Breakfast window)
        worker.overrideNotificationSender = { _, meal ->
            notificationSent = true
            triggeredMeal = meal
        }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(notificationSent) // Skipped because breakfast already logged
    }

    @Test
    fun mealReminder_firesWhenMealNotLoggedDuringMealWindow() = runBlocking {
        var notificationSent = false
        var triggeredMeal = ""
        val worker = TestListenableWorkerBuilder<MealReminderWorker>(context).build()
        worker.overrideMealLogDao = database.mealLogDao()
        worker.overrideUserPreferencesRepository = fakePrefs
        worker.overrideCurrentTime = LocalTime.of(12, 30) // 12:30 PM (Lunch window)
        worker.overrideNotificationSender = { _, meal ->
            notificationSent = true
            triggeredMeal = meal
        }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(notificationSent)
        assertEquals("Lunch", triggeredMeal)
    }

    @Test
    fun mealReminder_skipsOutsideMealWindows() = runBlocking {
        var notificationSent = false
        val worker = TestListenableWorkerBuilder<MealReminderWorker>(context).build()
        worker.overrideMealLogDao = database.mealLogDao()
        worker.overrideUserPreferencesRepository = fakePrefs
        worker.overrideCurrentTime = LocalTime.of(16, 0) // 4:00 PM (Between lunch & dinner)
        worker.overrideNotificationSender = { _, _ -> notificationSent = true }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(notificationSent) // Outside meal windows
    }

    @Test
    fun mealReminder_skipsWhenDisabledInPreferences() = runBlocking {
        val disabledPrefs = TestUserPreferencesRepository(mealRemindersOn = false)
        var notificationSent = false
        val worker = TestListenableWorkerBuilder<MealReminderWorker>(context).build()
        worker.overrideMealLogDao = database.mealLogDao()
        worker.overrideUserPreferencesRepository = disabledPrefs
        worker.overrideCurrentTime = LocalTime.of(13, 0)
        worker.overrideNotificationSender = { _, _ -> notificationSent = true }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(notificationSent) // Disabled by user preference
    }

    @Test
    fun mealReminder_firesAtMostOncePerMealTypePerDay_evenIfWorkerRunsMultipleTimes() = runBlocking {
        var notificationCount = 0
        var lastMealNotified = ""

        // Run 1: 12:30 PM (Lunch window) -> fires notification
        val worker1 = TestListenableWorkerBuilder<MealReminderWorker>(context).build()
        worker1.overrideMealLogDao = database.mealLogDao()
        worker1.overrideUserPreferencesRepository = fakePrefs
        worker1.overrideCurrentTime = LocalTime.of(12, 30)
        worker1.overrideNotificationSender = { _, meal ->
            notificationCount++
            lastMealNotified = meal
        }

        val result1 = worker1.doWork()
        assertEquals(ListenableWorker.Result.success(), result1)
        assertEquals(1, notificationCount)
        assertEquals("Lunch", lastMealNotified)

        // Run 2: 13:30 PM (still in Lunch window, still not logged) -> SKIPS notification due to persisted marker
        val worker2 = TestListenableWorkerBuilder<MealReminderWorker>(context).build()
        worker2.overrideMealLogDao = database.mealLogDao()
        worker2.overrideUserPreferencesRepository = fakePrefs
        worker2.overrideCurrentTime = LocalTime.of(13, 30)
        worker2.overrideNotificationSender = { _, meal ->
            notificationCount++
            lastMealNotified = meal
        }

        val result2 = worker2.doWork()
        assertEquals(ListenableWorker.Result.success(), result2)
        assertEquals(1, notificationCount) // Count MUST remain 1; no repeat notification

        // Run 3: 19:00 PM (Dinner window) -> fires notification for Dinner
        val worker3 = TestListenableWorkerBuilder<MealReminderWorker>(context).build()
        worker3.overrideMealLogDao = database.mealLogDao()
        worker3.overrideUserPreferencesRepository = fakePrefs
        worker3.overrideCurrentTime = LocalTime.of(19, 0)
        worker3.overrideNotificationSender = { _, meal ->
            notificationCount++
            lastMealNotified = meal
        }

        val result3 = worker3.doWork()
        assertEquals(ListenableWorker.Result.success(), result3)
        assertEquals(2, notificationCount) // Incremented to 2 for the distinct Dinner window
        assertEquals("Dinner", lastMealNotified)
    }
}