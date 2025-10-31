package dev.lizainslie.pitohui.modules.system.commands

import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand

val SettingsCommand = defineCommand(
    "settings",
    "View and edit your community's settings",
) {
    handle {
        respond("Provide a setting to view or edit")
    }

    subCommand(
        "modlog_channel",
        "The channel where moderation events are logged",
    ) {
        val channelArg = argument(
            "channel",
            "The channel where moderation events are logged",
            type = ArgumentTypes.CHANNEL
        )

        handle {
            val channel = args[channelArg]

            if (channel != null) respond("Modlog channel set to $channel")
            else respond("Modlog channel currently set to [#example-channel]")
        }
    }
}
