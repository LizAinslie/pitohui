package dev.lizainslie.pitohui.util.time

import kotlin.time.Duration

fun formatDuration(duration: Duration): String {
    duration.toComponents { hours, minutes, seconds, _ ->
        val parts = mutableListOf<String>()
        if (hours > 0) parts.add("$hours hour${if (hours > 1) "s" else ""}")
        if (minutes > 0) parts.add("$minutes minute${if (minutes > 1) "s" else ""}")
        if (seconds > 0 || parts.isEmpty()) parts.add("$seconds second${if (seconds > 1) "s" else ""}")
        return parts.joinToString(", ")
    }
}
