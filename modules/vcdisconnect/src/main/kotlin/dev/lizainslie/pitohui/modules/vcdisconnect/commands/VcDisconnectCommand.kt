package dev.lizainslie.pitohui.modules.vcdisconnect.commands

import dev.lizainslie.pitohui.core.commands.argument.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.modules.vcdisconnect.VcDisconnectModule
import dev.lizainslie.pitohui.modules.vcdisconnect.data.DisconnectTimer
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.enforceDiscord
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import dev.lizainslie.pitohui.util.time.format
import kotlinx.datetime.Clock

val VcDisconnectCommand = defineCommand(
    "vcdisconnect",
    "Have the bot disconnect you from a voice channel after a specified duration."
) {
    platform(Discord) {
        dmPermission = false
    }

    communityOnly = true

    subCommand("set", "Set a timer to disconnect from a voice channel.") {
        val durationArgument = argument(
            "duration",
            "The duration (in minutes) after which to disconnect from the voice channel.",
            ArgumentTypes.DURATION,
            required = true
        )

        handle {
            enforceDiscord {
                enforceGuild { guild ->
                    val member = guild.getMemberOrNull(callerId.snowflake) ?: return@enforceGuild
                    val voiceState = member.getVoiceStateOrNull() ?: run {
                        respondError("You must be in a voice channel to set a disconnect timer.")
                        return@enforceGuild
                    }

                    val channel = voiceState.channelId ?: return@enforceGuild
                    val duration = args[durationArgument] ?: return@enforceGuild

                    VcDisconnectModule.addDisconnectTimer(guild.id.platform, DisconnectTimer(
                        user = callerId,
                        channel = channel.platform,
                        disconnectAt = Clock.System.now() + duration
                    ))

                    respondPrivate("You will be disconnected from <#$channel> after ${duration.format()}.")
                }
            }
        }
    }

    subCommand("cancel", "Cancel a previously set disconnect timer.") {
        handle {
            enforceDiscord {
                enforceGuild { guild ->
                    VcDisconnectModule.clearDisconnectTimer(
                        communityId = guild.id.platform,
                        userId = callerId
                    )

                    respondPrivate("Your disconnect timer has been cancelled.")
                }
            }
        }
    }
}