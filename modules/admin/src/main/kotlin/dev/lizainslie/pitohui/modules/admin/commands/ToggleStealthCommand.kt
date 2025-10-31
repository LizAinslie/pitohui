package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.data.DeveloperOptions
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.platforms.discord.extensions.DISCORD
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val ToggleStealthCommand = defineCommand(
    "toggle_stealth",
    "Toggle stealth / vanish mode for bot developers"
) {
    platforms(Platforms.DISCORD)

    handle {
        newSuspendedTransaction {
            val devOptions = DeveloperOptions.getDeveloperOptions(callerId)
                ?: return@newSuspendedTransaction // exit silently if user isn't dev.

            devOptions.stealth = !devOptions.stealth

            respondPrivate("Stealth mode is now `${if (devOptions.stealth) "on" else "off"}`.")
        }
    }
}