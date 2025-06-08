package dev.lizainslie.pitohui.modules.system.commands

import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand

val SettingsCommand = defineCommand(
    "settings",
    "View and edit your community's settings",
) {
    handle { context ->
        context.respond("Provide a setting to view or edit")
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

        handle { context ->
            val channel = channelArg.resolve(context)

            if (channel != null) context.respond("Modlog channel set to ${channel.mention}")
            else context.respond("Modlog channel currently set to [#example-channel]")
        }
    }
}