package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.moeka.core.commands.defineCommand
import dev.lizainslie.moeka.core.data.entities.DeveloperOptions
import dev.lizainslie.moeka.platforms.discord.Discord
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val ToggleStealthCommand =
    defineCommand(
        "toggle_stealth",
        "Toggle stealth / vanish mode for bot developers",
    ) {
        platform(Discord)

        handle {
            newSuspendedTransaction {
                val devOptions =
                    DeveloperOptions.getDeveloperOptions(callerId)
                        ?: return@newSuspendedTransaction // exit silently if user isn't dev.

                devOptions.stealth = !devOptions.stealth

                respondPrivate("Stealth mode is now `${if (devOptions.stealth) "on" else "off"}`.")
            }
        }
    }
