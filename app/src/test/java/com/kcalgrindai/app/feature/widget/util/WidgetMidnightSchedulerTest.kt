package com.kcalgrindai.app.feature.widget.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class WidgetMidnightSchedulerTest {

    @Test
    fun testCalculateNextMidnightEpochMillisInUtc() {
        val now = Instant.parse("2026-10-10T14:30:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val scheduler = WidgetMidnightScheduler(clock)

        val nextMidnight = scheduler.calculateNextMidnightEpochMillis(ZoneOffset.UTC)
        val expected = Instant.parse("2026-10-11T00:00:00Z").toEpochMilli()

        assertEquals(expected, nextMidnight)
    }

    @Test
    fun testCalculateNextMidnightEpochMillisWithTimezoneOffset() {
        // 2026-10-10 14:00 UTC is 2026-10-10 23:00 in Asia/Tokyo (UTC+9)
        val now = Instant.parse("2026-10-10T14:00:00Z")
        val tokyoZone = ZoneId.of("Asia/Tokyo")
        val clock = Clock.fixed(now, tokyoZone)
        val scheduler = WidgetMidnightScheduler(clock)

        val nextMidnight = scheduler.calculateNextMidnightEpochMillis(tokyoZone)
        // Midnight in Tokyo is 2026-10-11 00:00:00 JST = 2026-10-10 15:00:00 UTC (1 hour from now!)
        val expected = Instant.parse("2026-10-10T15:00:00Z").toEpochMilli()

        assertEquals(expected, nextMidnight)
    }
}
