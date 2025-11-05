package dev.lizainslie.pitohui.platforms.discord.commands

import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Clock

fun EmbedBuilder.buildExceptionEmbed(e: Exception) {
    title = "An error occurred while running this command!"
    description = """${e.message}
                    |```kt
                    |${e.stackTraceToString()}
                    |```
                """.trimMargin()
    timestamp = Clock.System.now()
    color = Color(0xdc3545)
}