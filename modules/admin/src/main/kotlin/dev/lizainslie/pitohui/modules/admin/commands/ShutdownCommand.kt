package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.moeka.core.commands.argument.ArgumentTypes
import dev.lizainslie.moeka.core.commands.defineCommand
import dev.lizainslie.moeka.core.platforms.entities.PlatformResponse
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.moeka.util.std.enumSetAll
import dev.lizainslie.moeka.util.time.format
import dev.lizainslie.pitohui.modules.admin.AdminModule
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal val DEFAULT_SHUTDOWN_DELAY = 0.milliseconds

val ShutdownCommand =
    defineCommand(
        name = "shutdown",
        description = "Shuts down the bot.",
    ) {
        platform(Discord)

        val delayArgument =
            argument(
                "after",
                "Delay before shutdown",
                ArgumentTypes.DURATION,
            ) {
                defaultValue = DEFAULT_SHUTDOWN_DELAY
            }

        handle {
            val delay by delayArgument.require()

            var response: PlatformResponse? = null

            if (delay > 0.milliseconds) {
                response = respondPrivate("Shutting down pitohui in ${delay.format(enumSetAll())}...")
            }

            delay(delay)

            response?.createPrivateFollowup("Shutting down pitohui.")
                ?: respondPrivate("Shutting down pitohui.")

            AdminModule.shutdownBot()
        }
    }
