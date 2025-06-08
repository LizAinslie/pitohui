package dev.lizainslie.pitohui.core.commands

fun interface CommandHandler {
    suspend fun handle(context: CommandContext)
}