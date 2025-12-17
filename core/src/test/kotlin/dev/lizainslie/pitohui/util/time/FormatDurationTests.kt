package dev.lizainslie.pitohui.util.time

import java.util.EnumSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class FormatDurationTests {
    @Test
    fun `single part durations should format as '{amount} {unit}(s)'`() {
        val durationSecond = 1.seconds
        val durationSecondFormatted = durationSecond.format()
        assertEquals("1 second", durationSecondFormatted)

        val durationSeconds = 5.seconds
        val durationSecondsFormatted = durationSeconds.format()
        assertEquals("5 seconds", durationSecondsFormatted)

        val durationMinute = 1.minutes
        val durationMinuteFormatted = durationMinute.format()
        assertEquals("1 minute", durationMinuteFormatted)

        val durationMinutes = 10.minutes
        val durationMinutesFormatted = durationMinutes.format()
        assertEquals("10 minutes", durationMinutesFormatted)

        val durationHour = 1.hours
        val durationHourFormatted = durationHour.format()
        assertEquals("1 hour", durationHourFormatted)

        val durationHours = 3.hours
        val durationHoursFormatted = durationHours.format()
        assertEquals("3 hours", durationHoursFormatted)
    }

    @Test
    fun `durations with many parts should display hours, minutes, and seconds`() {
        val duration = 1.hours + 2.minutes + 3.seconds
        val durationFormatted = duration.format()
        assertEquals("1 hour, 2 minutes, 3 seconds", durationFormatted)
    }

    @Test
    fun `days and nanoseconds should not be formatted unless the units contains DurationUnit$DAYS or DurationUnit$NANOSECONDS`() {
        val duration = 1.days + 2.hours + 30.minutes
        val formattedWithoutDays = duration.format()
        assertEquals("2 hours, 30 minutes", formattedWithoutDays)

        val formattedWithDays = duration.format(EnumSet.of(DurationUnit.DAYS, DurationUnit.HOURS, DurationUnit.MINUTES))
        assertEquals("1 day, 2 hours, 30 minutes", formattedWithDays)

        val durationWithNanos = 2.seconds + 500.nanoseconds
        val formattedWithoutNanos = durationWithNanos.format()
        assertEquals("2 seconds", formattedWithoutNanos)

        val formattedWithNanos = durationWithNanos.format(EnumSet.of(DurationUnit.SECONDS, DurationUnit.NANOSECONDS))
        assertEquals("2 seconds, 500 nanoseconds", formattedWithNanos)
    }
}
