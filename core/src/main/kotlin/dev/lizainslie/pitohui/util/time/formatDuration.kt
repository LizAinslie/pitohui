package dev.lizainslie.pitohui.util.time

import java.util.EnumSet
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Formats a [Duration] into a human-readable string.
 *
 * @param units The set of [DurationUnit] to include in the formatted string. Defaults to hours, minutes, and seconds.
 * @return A formatted string representing the duration.
 */
fun Duration.format(
    units: EnumSet<DurationUnit> =
        EnumSet.of(DurationUnit.HOURS, DurationUnit.MINUTES, DurationUnit.SECONDS)
) =
    toComponents { days, hours, minutes, seconds, nanoseconds ->
        val parts = mutableListOf<String>()
        if (units.contains(DurationUnit.DAYS) && days > 0) parts.add("$days day${if (days > 1) "s" else ""}")
        if (units.contains(DurationUnit.HOURS) && hours > 0) parts.add("$hours hour${if (hours > 1) "s" else ""}")
        if (units.contains(DurationUnit.MINUTES) && minutes > 0) parts.add("$minutes minute${if (minutes > 1) "s" else ""}")
        if (units.contains(DurationUnit.SECONDS) && seconds > 0 || parts.isEmpty()) parts.add("$seconds second${if (seconds > 1) "s" else ""}")
        if (units.contains(DurationUnit.NANOSECONDS) && nanoseconds > 0) parts.add("$nanoseconds nanosecond${if (nanoseconds > 1) "s" else ""}")
        parts.joinToString(", ")
    }
