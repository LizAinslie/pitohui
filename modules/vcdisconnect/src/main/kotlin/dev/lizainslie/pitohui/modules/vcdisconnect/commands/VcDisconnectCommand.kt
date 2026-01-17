package dev.lizainslie.pitohui.modules.vcdisconnect.commands

import dev.lizainslie.moeka.core.commands.argument.ArgumentTypes
import dev.lizainslie.moeka.core.commands.defineCommand
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.moeka.platforms.discord.commands.enforceDiscord
import dev.lizainslie.moeka.platforms.discord.extensions.platform
import dev.lizainslie.moeka.platforms.discord.extensions.snowflake
import dev.lizainslie.moeka.util.time.format
import dev.lizainslie.pitohui.modules.vcdisconnect.VcDisconnectModule
import dev.lizainslie.pitohui.modules.vcdisconnect.data.DisconnectTimer
import kotlinx.datetime.Clock

val VcDisconnectCommand =
    defineCommand(
        "vcdisconnect",
        "Have the bot disconnect you from a voice channel after a specified duration.",
    ) {
        platform(Discord) {
            dmPermission = false
        }

        communityOnly = true

        subCommand("set", "Set a timer to disconnect from a voice channel.") {
            val durationArgument =
                argument(
                    "duration",
                    "The duration (in minutes) after which to disconnect from the voice channel.",
                    ArgumentTypes.DURATION,
                ) {
                    required = true
                }

            handle {
                val duration by durationArgument.require()

                enforceDiscord {
                    enforceGuild { guild ->
                        val member = guild.getMemberOrNull(callerId.snowflake) ?: return@enforceGuild
                        val voiceState =
                            member.getVoiceStateOrNull() ?: run {
                                respondError("You must be in a voice channel to set a disconnect timer.")
                                return@enforceGuild
                            }

                        val channel = voiceState.channelId ?: return@enforceGuild

                        VcDisconnectModule.addDisconnectTimer(
                            guild.id.platform,
                            DisconnectTimer(
                                user = callerId,
                                channel = channel.platform,
                                disconnectAt = Clock.System.now() + duration,
                            ),
                        )

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
                            userId = callerId,
                        )

                        respondPrivate("Your disconnect timer has been cancelled.")
                    }
                }
            }
        }
    }
