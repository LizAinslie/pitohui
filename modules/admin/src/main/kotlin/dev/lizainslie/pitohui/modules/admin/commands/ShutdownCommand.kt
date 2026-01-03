package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.argument.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.platforms.entities.PlatformResponse
import dev.lizainslie.pitohui.modules.admin.AdminModule
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.util.std.enumSetAll
import dev.lizainslie.pitohui.util.time.format
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal val DEFAULT_SHUTDOWN_DELAY = 0.milliseconds

val ShutdownCommand = defineCommand(
    name = "shutdown",
    description = "Shuts down the bot.",
) {
    platform(Discord)

    val delayArgument = argument(
        "after",
        "Delay before shutdown",
        ArgumentTypes.DURATION
    ) {
        defaultValue = DEFAULT_SHUTDOWN_DELAY
    }

    handle {
        val delay by delayArgument.require()

        var response: PlatformResponse? = null

        if (delay > 0.milliseconds)
            response = respondPrivate("Shutting down pitohui in ${delay.format(enumSetAll())}...")

        delay(delay)

        response?.createPrivateFollowup("Shutting down pitohui.")
            ?: respondPrivate("Shutting down pitohui.")

        AdminModule.shutdownBot()
    }
}