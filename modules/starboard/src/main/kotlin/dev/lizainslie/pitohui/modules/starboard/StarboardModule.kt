package dev.lizainslie.pitohui.modules.starboard

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.rest.Image
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.lizainslie.moeka.core.Bot
import dev.lizainslie.moeka.core.modules.AbstractModule
import dev.lizainslie.moeka.core.modules.ModuleVisibility
import dev.lizainslie.moeka.core.platforms.PlatformId
import dev.lizainslie.moeka.core.platforms.SupportPlatforms
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.moeka.platforms.discord.extensions.getIdentifier
import dev.lizainslie.moeka.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.modules.starboard.commands.StarboardCommand
import dev.lizainslie.pitohui.modules.starboard.data.entities.Starboard
import dev.lizainslie.pitohui.modules.starboard.data.entities.StarboardEntry
import dev.lizainslie.pitohui.modules.starboard.data.tables.StarboardEntryTable
import dev.lizainslie.pitohui.modules.starboard.data.tables.StarboardTable
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@SupportPlatforms(Discord::class)
object StarboardModule : AbstractModule(
    name = "starboard",
    description = "Highlight popular messages in a dedicated channel.",
    visibility = ModuleVisibility.MODERATOR,
    commands =
        setOf(
            StarboardCommand,
        ),
    tables =
        setOf(
            StarboardTable,
            StarboardEntryTable,
        ),
) {
    suspend fun emojiRepresentation(
        emoji: String,
        guild: Guild,
    ): String? =
        if (emoji.all { it.isDigit() }) {
            val discordEmoji = guild.getEmojiOrNull(Snowflake(emoji))
            discordEmoji?.mention
        } else {
            emoji
        }

    suspend fun createReactionEmojiFromRepresentation(
        emoji: String,
        guild: Guild,
    ): ReactionEmoji? =
        if (emoji.all { it.isDigit() }) {
            val discordEmoji = guild.getEmojiOrNull(Snowflake(emoji))
            discordEmoji?.let {
                ReactionEmoji.Custom(
                    it.id,
                    it.name!!,
                    isAnimated = it.isAnimated,
                )
            }
        } else {
            ReactionEmoji.Unicode(emoji)
        }

    override fun onInit(bot: Bot) {
        super.onInit(bot)

        Discord.addEventListener<ReactionAddEvent> {
            if (guildId == null) return@addEventListener // only care about guilds for starboard

            val starboard = getStarboard(guildId!!.platform, emoji.getIdentifier()) ?: return@addEventListener

            if (!starboard.selfStarAllowed && userId == message.fetchMessage().author?.id) return@addEventListener
            val stars = countStars(message, emoji, starboard, userId)

            if (channelId.value.toString() == starboard.channelId.value) {
                if (userId == Discord.myId) return@addEventListener

                log.debug("reacted message is in starboard channel, checking for existing entry")
                val existingEntry =
                    findStarboardEntryFromStarboardMessage(
                        communityId = guildId!!.platform,
                        channelId = channelId.platform,
                        messageId = message.id.platform,
                    )

                if (existingEntry != null) {
                    log.debug("found existing starboard entry for reacted message")
                    // message reacted to is the starboard entry itself, check if the user has starred it on the original message and update accordingly

                    val originalChannel = Discord.getChannelById(existingEntry.channelId) ?: return@addEventListener
                    if (originalChannel is MessageChannel) {
                        originalChannel.getMessageOrNull(Snowflake(existingEntry.messageId.value))?.let { originalMessage ->
                            val reactors = originalMessage.getReactors(emoji).toList()
                            val userHasStarredOriginal = reactors.any { it.id == userId }

                            if (userHasStarredOriginal) {
                                // user has starred the original message, remove and ignore
                                message.deleteReaction(userId, emoji)
                                return@addEventListener
                            }

                            // user has not starred the original message, increment star count

                            newSuspendedTransaction {
                                existingEntry.starCount++
                            }

                            editStarboardEmbed(starboard, existingEntry)
                        }
                    }

                    return@addEventListener
                }
            } else {
                log.debug("reacted message is not in starboard channel, checking for existing entry")
                // message reacted to is an original message, check if it already has a starboard entry

                val existingEntry =
                    findStarboardEntryFromOriginalMessage(
                        communityId = guildId!!.platform,
                        starboardChannelId = PlatformId(Discord.key, starboard.channelId.value),
                        messageId = message.id.platform,
                    )

                if (existingEntry != null) {
                    log.debug("found existing starboard entry for reacted message")
                    // message already has a starboard entry, just update the star count
                    newSuspendedTransaction {
                        existingEntry.starCount++
                    }

                    editStarboardEmbed(starboard, existingEntry)

                    return@addEventListener
                } else {
                    log.debug("no existing starboard entry for reacted message")
                    // message does not have a starboard entry yet, will need to create one if threshold is met

                    if (stars >= starboard.starThreshold) {
                        val starboardMessageId =
                            postNewStarboardEmbed(
                                starboard = starboard,
                                message = message.fetchMessage(),
                                starCount = stars,
                            )

                        log.debug("posted new starboard embed for message, id: $starboardMessageId")

                        if (starboardMessageId != null) {
                            newSuspendedTransaction {
                                StarboardEntry.create(
                                    communityId = guildId!!.platform,
                                    starboardChannelId = PlatformId(Discord.key, starboard.channelId.value),
                                    starboardChannelMessageId = starboardMessageId.value.toString(),
                                    channelId = channelId.platform,
                                    messageId = message.id.platform,
                                    starCount = stars,
                                )
                            }
                        }
                    }
                }
            }
        }

        Discord.addEventListener<ReactionRemoveEvent> {
            if (guildId == null) return@addEventListener // only care about guilds for starboard

            val starboard = getStarboard(guildId!!.platform, emoji.getIdentifier()) ?: return@addEventListener

            if (!starboard.selfStarAllowed && userId == message.fetchMessage().author?.id) return@addEventListener
            val stars = countStars(message, emoji, starboard, userId)

            if (channelId.value.toString() == starboard.channelId.value) {
                if (userId == Discord.myId) return@addEventListener // ignore bot removals in starboard channel

                val entry =
                    findStarboardEntryFromStarboardMessage(
                        communityId = guildId!!.platform,
                        channelId = channelId.platform,
                        messageId = message.id.platform,
                    ) ?: return@addEventListener

                // todo: decrement star count, update starboard embed, etc.

                newSuspendedTransaction {
                    entry.starCount--
                }

                if (entry.starCount < starboard.starThreshold) {
                    message.delete()
                    newSuspendedTransaction {
                        entry.delete()
                    }
                } else {
                    editStarboardEmbed(starboard, entry)
                }
            } else {
                val entry =
                    findStarboardEntryFromOriginalMessage(
                        communityId = guildId!!.platform,
                        starboardChannelId = PlatformId(Discord.key, starboard.channelId.value),
                        messageId = message.id.platform,
                    ) ?: return@addEventListener

                newSuspendedTransaction {
                    entry.starCount--
                }

                if (entry.starCount < starboard.starThreshold) {
                    Discord.getChannelById(starboard.channelId.value)?.let { channel ->
                        if (channel is MessageChannel) {
                            val starboardMessage = channel.getMessage(Snowflake(entry.starboardChannelMessageId.value))
                            starboardMessage.delete()
                        }
                    }

                    newSuspendedTransaction {
                        entry.delete()
                    }
                } else {
                    editStarboardEmbed(starboard, entry)
                }
            }
        }
    }

    suspend fun getStarboard(
        communityId: PlatformId,
        emoji: String,
    ) = newSuspendedTransaction {
        Starboard.findByReactionForCommunity(communityId, emoji)
    }

    suspend fun countStars(
        message: MessageBehavior,
        emoji: ReactionEmoji,
        starboard: Starboard,
        userId: Snowflake,
    ): Int {
        val reactors = message.getReactors(emoji)

        return if (!starboard.selfStarAllowed) {
            reactors.filter { it.id != userId }.count()
        } else {
            reactors.count()
        }
    }

    suspend fun findStarboardEntryFromStarboardMessage(
        communityId: PlatformId,
        channelId: PlatformId,
        messageId: PlatformId,
    ) = newSuspendedTransaction {
        StarboardEntry.findByStarboardMessageId(
            communityId = communityId,
            starboardChannelId = channelId,
            starboardChannelMessageId = messageId,
        )
    }

    suspend fun findStarboardEntryFromOriginalMessage(
        communityId: PlatformId,
        starboardChannelId: PlatformId,
        messageId: PlatformId,
    ) = newSuspendedTransaction {
        StarboardEntry.findByMessageId(
            communityId = communityId,
            starboardChannelId = starboardChannelId,
            messageId = messageId,
        )
    }

    suspend fun postNewStarboardEmbed(
        starboard: Starboard,
        message: Message,
        starCount: Int,
    ) = Discord.getGuildById(starboard.communityId.value)?.let { guild ->
        val emojiRep = emojiRepresentation(starboard.emoji, guild) ?: starboard.emoji
        Discord.getChannelById(starboard.channelId.value)?.let { channel ->
            if (channel is MessageChannel) {
                val newMessage =
                    channel.createMessage {
                        embed {
                            buildStarboardEmbed(starboard.communityId.value, message, starCount, emojiRep)
                        }

                        // copy over original message embeds
                        if (message.embeds.isNotEmpty()) {
                            for (messageEmbed in message.embeds) {
                                embed {
                                    title = messageEmbed.title
                                    description = messageEmbed.description
                                    url = messageEmbed.url
                                    color = messageEmbed.color
                                    timestamp = messageEmbed.timestamp

                                    messageEmbed.fields.forEach { field ->
                                        field {
                                            name = field.name
                                            value = field.value
                                            inline = field.inline
                                        }
                                    }

                                    messageEmbed.footer?.let {
                                        footer {
                                            text = it.text
                                            icon = it.iconUrl
                                        }
                                    }

                                    messageEmbed.image?.url?.let {
                                        image = it
                                    }

                                    messageEmbed.thumbnail?.url?.let {
                                        thumbnail {
                                            url = it
                                        }
                                    }

                                    messageEmbed.author?.let {
                                        author {
                                            name = it.name
                                            url = it.url
                                            icon = it.iconUrl
                                        }
                                    }
                                }
                            }
                        }
                    }

                val reactionEmoji = createReactionEmojiFromRepresentation(starboard.emoji, guild)
                if (reactionEmoji != null) newMessage.addReaction(reactionEmoji)

                newMessage.id
            } else {
                null
            }
        }
    }

    suspend fun editStarboardEmbed(
        starboard: Starboard,
        entry: StarboardEntry,
    ) {
        Discord.getGuildById(starboard.communityId.value)?.let { guild ->
            val emojiRep = emojiRepresentation(starboard.emoji, guild) ?: starboard.emoji
            Discord.getChannelById(starboard.channelId.value)?.let { channel ->
                if (channel is MessageChannel) {
                    val starboardMessage = channel.getMessage(Snowflake(entry.starboardChannelMessageId.value))
                    val originalMessage =
                        Discord.getChannelById(entry.channelId)?.let { originalChannel ->
                            if (originalChannel is MessageChannel) {
                                originalChannel.getMessage(Snowflake(entry.messageId.value))
                            } else {
                                null
                            }
                        } ?: return

                    starboardMessage.edit {
                        embed {
                            buildStarboardEmbed(starboard.communityId.value, originalMessage, entry.starCount, emojiRep)
                        }
                    }
                }
            }
        }
    }

    fun EmbedBuilder.buildStarboardEmbed(
        communityId: String,
        originalMessage: Message,
        starCount: Int,
        emoji: String = "⭐",
    ) {
        author {
            name = originalMessage.author?.username
            icon =
                originalMessage.author?.avatar?.cdnUrl?.toUrl {
                    size = Image.Size.Size32
                    format = Image.Format.PNG
                }
        }

        title = "$emoji $starCount"

        description = originalMessage.content

        if (originalMessage.embeds.isNotEmpty()) {
            description += "[${originalMessage.embeds.size} embed${if (originalMessage.embeds.size > 1) "s" else ""}]"
        }

        description +=
            "\n\n[Jump to Message](https://discord.com/channels/\$communityId/\${originalMessage.channelId.value}/\${originalMessage.id.value})"

        if (originalMessage.attachments.isNotEmpty()) {
            image = originalMessage.attachments.first().url
        }

        footer {
            text = "Message ID: ${originalMessage.id.value}"
        }
        timestamp = originalMessage.timestamp
    }
}
