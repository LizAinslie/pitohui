package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.data.DeveloperOptions
import dev.lizainslie.pitohui.platforms.discord.Discord
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val DumpContextCommand = defineCommand(
    "context_debug",
    "Enable context debugging to dump the command context for inspection"
) {
    platforms(Discord)

    handle {
        newSuspendedTransaction {
            val devOptions = DeveloperOptions.getDeveloperOptions(callerId)
                ?: return@newSuspendedTransaction // exit silently if user isn't dev.

            devOptions.contextDebug = !devOptions.contextDebug

            respondPrivate("Context debugging mode is now `${if (devOptions.contextDebug) "on" else "off"}`.")
        }
    }
}