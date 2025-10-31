package dev.lizainslie.pitohui.util.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FormatDurationTests {
    @Test
    fun testFormatDurationBasic() {
        val durationSecond = 1.seconds
        val durationSecondFormatted = formatDuration(durationSecond)
        assertEquals("1 second", durationSecondFormatted)

        val durationMinute = 1.minutes
        val durationMinuteFormatted = formatDuration(durationMinute)
        assertEquals("1 minute", durationMinuteFormatted)

        val durationHour = 1.hours
        val durationHourFormatted = formatDuration(durationHour)
        assertEquals("1 hour", durationHourFormatted)
    }

    @Test
    fun testFormatDurationComplex() {
        val duration = 1.hours + 2.minutes + 3.seconds
        val durationFormatted = formatDuration(duration)
        assertEquals("1 hour, 2 minutes, 3 seconds", durationFormatted)
    }
}
