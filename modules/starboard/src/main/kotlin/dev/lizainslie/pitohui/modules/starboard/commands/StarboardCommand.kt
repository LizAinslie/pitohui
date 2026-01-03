package dev.lizainslie.pitohui.modules.starboard.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.channel.MessageChannel
import dev.lizainslie.pitohui.core.commands.argument.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.modules.starboard.StarboardModule
import dev.lizainslie.pitohui.modules.starboard.data.entities.Starboard
import dev.lizainslie.pitohui.modules.starboard.data.tables.StarboardTable
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordCommandContext
import dev.lizainslie.pitohui.platforms.discord.commands.enforceDiscordType
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val StarboardCommand = defineCommand(
    name = "starboard",
    description = "Manage the starboards in this community.",
) {
    platform(Discord) {
        defaultMemberPermissions = Permissions(Permission.ManageGuild)
        dmPermission = false
    }

    communityOnly = true

    subCommand("create", "Create a starboard in this community.") {
        val channelArg = argument("channel", "The channel to use as the starboard.", ArgumentTypes.CHANNEL) {
            required = true
        }

        val thresholdArg = argument("threshold", "The number of stars required to post a message to the starboard.", ArgumentTypes.INT) {
            defaultValue = 5
        }

        val emojiArg = argument("emoji", "The emoji to use for starring messages.", ArgumentTypes.STRING) {
            defaultValue = "⭐️"
        }

        val selfStarArg = argument("self_star", "Whether to allow users to star their own messages.", ArgumentTypes.BOOLEAN) {
            defaultValue = false
        }

        handle {
            val channel by channelArg.require()
            val threshold by thresholdArg.require()
            val emojiRaw by emojiArg.require()
            val emoji = Discord.parseEmojiIdentifier(emojiRaw)
            val selfStar by selfStarArg.require()

            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->
                    val response = respondPrivate("Creating starboard...")
                    val communityId = guild.id.platform

                    val existingStarboard = newSuspendedTransaction {
                        Starboard.findByCreateArgs(
                            communityId = communityId,
                            channelId = channel,
                        )
                    }

                    if (existingStarboard != null) {
                        response.createPrivateFollowup("A starboard already exists for that channel.")
                        return@enforceGuild
                    }

                    if (newSuspendedTransaction { Starboard.existsByEmoji(communityId, emoji) }) {
                        response.createPrivateFollowup("A starboard already exists with that emoji.")
                        return@enforceGuild
                    }

                    val discordChannel = Discord.getChannelById(channel)

                    if (discordChannel == null) {
                        response.createPrivateFollowup("The specified channel does not exist.")
                        return@enforceGuild
                    }

                    if (discordChannel !is MessageChannel) {
                        response.createPrivateFollowup("The specified channel is not a message channel.")
                        return@enforceGuild
                    }

                    val emojiRep = StarboardModule.emojiRepresentation(emoji, guild)
                    if (emojiRep == null) {
                        response.createPrivateFollowup("The specified emoji does not exist in this community.")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        Starboard.create(
                            communityId = communityId,
                            channelId = channel,
                            starThreshold = threshold,
                            emoji = emoji,
                            selfStarAllowed = selfStar,
                        )
                    }

                    response.createFollowup("Starboard created successfully in channel ${discordChannel.mention} with threshold $threshold, emoji $emojiRep, self-star allowed: $selfStar.")
                }
            }
        }
    }

    subCommand("remove", "Remove the starboard from this community.") {
        val channelArg = argument("channel", "The starboard channel to remove.", ArgumentTypes.CHANNEL) {
            required = true
        }

        handle {
            val channel by channelArg.require()

            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->
                    val communityId = guild.id.platform

                    val existingStarboard = newSuspendedTransaction {
                        Starboard.findByCreateArgs(
                            communityId = communityId,
                            channelId = channel,
                        )
                    }
                    if (existingStarboard == null) {
                        respondError("No starboard exists for that channel.")
                        return@enforceGuild
                    }
                    newSuspendedTransaction {
                        existingStarboard.delete()
                    }
                    respond("Starboard in channel <#${channel.id}> has been removed.")
                }
            }
        }
    }

    subCommand("list", "List the starboards in this community.") {
        handle {
            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->
                    val communityId = guild.id.platform

                    val starboards = newSuspendedTransaction {
                        Starboard.find {
                            (StarboardTable.platform eq communityId.platform.key) and
                            (StarboardTable.communityId eq communityId.id)
                        }.toList()
                    }

                    if (starboards.isEmpty()) {
                        respond("No starboards are configured for this community.")
                        return@enforceGuild
                    }

                    var starboardList = ""

                    for (starboard in starboards) {
                        val channel = Discord.getChannelById(starboard.channelId.value)
                        val emojiRep = StarboardModule.emojiRepresentation(starboard.emoji, guild) ?: starboard.emoji
                        starboardList += "\nChannel: ${channel?.mention ?: "<#${starboard.channelId}>"}, Threshold: ${starboard.starThreshold}, Emoji: $emojiRep, Self-Star Allowed: ${starboard.selfStarAllowed}"
                    }

                    respond("# Starboards in this community:$starboardList")
                }
            }
        }
    }
}
