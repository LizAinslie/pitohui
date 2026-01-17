package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.moeka.core.commands.defineCommand
import dev.lizainslie.moeka.core.data.entities.DeveloperOptions
import dev.lizainslie.moeka.platforms.discord.Discord
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val DumpContextCommand =
    defineCommand(
        "context_debug",
        "Enable context debugging to dump the command context for inspection",
    ) {
        platform(Discord)

        handle {
            newSuspendedTransaction {
                val devOptions =
                    DeveloperOptions.getDeveloperOptions(callerId)
                        ?: return@newSuspendedTransaction // exit silently if user isn't dev.

                devOptions.contextDebug = !devOptions.contextDebug

                respondPrivate("Context debugging mode is now `${if (devOptions.contextDebug) "on" else "off"}`.")
            }
        }
    }
