package dev.lizainslie.pitohui.modules.greeting

// todo: configuration commands
// todo: test commands (/greeting test @user) that manually trigger the handlers

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.rest.builder.message.addFile
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.placeholder.placeholders
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.greeting.commands.GreetingModuleConfigCommand
import dev.lizainslie.pitohui.modules.greeting.data.entities.CommunityGreetingSettings
import dev.lizainslie.pitohui.modules.greeting.data.tables.CommunityGreetingSettingsTable
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.extensions.kordColor
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import kotlinx.datetime.Clock
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.awt.Color
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.exposed.sql.Except

@SupportPlatforms(Discord::class)
object GreetingModule : AbstractModule(
    name = "greeting",
    description = "A module that provides greeting commands.",
    commands = setOf(
        GreetingModuleConfigCommand
    ),
    tables = setOf(
        CommunityGreetingSettingsTable
    )
) {
    override fun onInit(bot: Bot) {
        super.onInit(bot)

        Discord.addEventListener<MemberJoinEvent> {
             handleMemberJoin(
                 communityId = guildId.platform,
                 userId = member.id
             )
        }

        Discord.addEventListener<MemberLeaveEvent> {
            handleMemberLeave(
                communityId = guildId.platform,
                userId = user.id
            )
        }
    }

    /**
     * Handle the [MemberJoinEvent] and send a welcome message if configured.
     *
     * @param communityId The ID of the community where the member joined.
     * @param userId The Snowflake ID of the member that joined.
     */
    suspend fun handleMemberJoin(communityId: PlatformId, userId: Snowflake) {
        val settings = findSettings(communityId) ?: return

        val channel = settings.welcomeChannelId?.let {
            Discord.getChannelById(it)
        } ?: return

        val placeholders = placeholders {
            replace("user_mention", "<@$userId>")
        }

        if (channel is MessageChannel) {
            withTempContextSuspend {
                channel.createMessage {
                    if (settings.embedWelcome) {
                        embed {
                            settings.welcomeMessage?.let {
                                description = placeholders.replace(it)
                            }

                            settings.welcomeColor?.let {
                                color = Color.decode(it).kordColor
                            }

                            settings.welcomeImageUrl?.let {
                                image = it
                            }

                            timestamp = Clock.System.now()
                        }
                    } else {
                        settings.welcomeMessage?.let {
                            content = placeholders.replace(it)
                        }

                        settings.welcomeImageUrl?.let {
                            val tmpImg = file("welcome-image_${communityId.id}")

                            val httpClient = OkHttpClient()
                            val request = Request.Builder()
                                .url(it)
                                .build()

                            httpClient.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) {
                                    log.warn("Received HTTP ${response.code} trying to download welcome image")
                                    return@let
                                }

                                val body = response.body ?: run {
                                    log.warn("Downloaded welcome image but there's no body (wtf??)")
                                    return@let
                                }

                                val bodyType = body.contentType()
                                    ?: response.header("Content-Type")?.toMediaTypeOrNull()
                                    ?: run {
                                        log.warn("Downloaded welcome image but cannot determine image type")
                                        return@let
                                    }

                                if (bodyType.type != "image") {
                                    log.warn("Downloaded welcome image but non-image content detected")
                                    return@let
                                }

                                val imgFileExt = when (bodyType.subtype) {
                                    "png" -> "png"
                                    "jpg", "jpeg" -> "jpg"
                                    "gif" -> "gif"
                                    "webp" -> "webp"
                                    else -> throw Exception("Welcome image is unknown image subtype ${bodyType.subtype}")
                                }

                                tmpImg.outputStream().use { fileOut ->
                                    body.byteStream().use { fileIn ->
                                        fileIn.copyTo(fileOut)
                                    }
                                }

                                addFile(tmpImg.toPath()) {
                                    filename = "welcome-image.$imgFileExt"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle the [MemberLeaveEvent] and send a goodbye message if configured.
     *
     * @param communityId The ID of the community where the member left.
     * @param userId The Snowflake ID of the member that left.
     */
    suspend fun handleMemberLeave(communityId: PlatformId, userId: Snowflake) {
        val settings = findSettings(communityId) ?: return

        val channel = settings.goodbyeChannelId?.let {
            Discord.getChannelById(it)
        } ?: return

        val placeholders = placeholders {
            replace("user_mention", "<@$userId>")
        }

        if (channel is MessageChannel) {
            withTempContextSuspend {
                channel.createMessage {
                    if (settings.embedGoodbye) {
                        embed {
                            settings.goodbyeMessage?.let {
                                description = placeholders.replace(it)
                            }

                            settings.goodbyeColor?.let {
                                color = Color.decode(it).kordColor
                            }

                            settings.goodbyeImageUrl?.let {
                                image = it
                            }

                            timestamp = Clock.System.now()
                        }
                    } else {
                        settings.goodbyeMessage?.let {
                            content = placeholders.replace(it)
                        }

                        settings.goodbyeImageUrl?.let {
                            val tmpImg = file("goodbye-image_${communityId.id}")

                            val httpClient = OkHttpClient()
                            val request = Request.Builder()
                                .url(it)
                                .build()

                            httpClient.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) {
                                    log.warn("Received HTTP ${response.code} trying to download goodbye image")
                                    return@let
                                }

                                val body = response.body ?: run {
                                    log.warn("Downloaded goodbye image but there's no body (wtf??)")
                                    return@let
                                }

                                val bodyType = body.contentType()
                                    ?: response.header("Content-Type")?.toMediaTypeOrNull()
                                    ?: run {
                                        log.warn("Downloaded goodbye image but cannot determine image type")
                                        return@let
                                    }

                                if (bodyType.type != "image") {
                                    log.warn("Downloaded goodbye image but non-image content detected")
                                    return@let
                                }

                                val imgFileExt = when (bodyType.subtype) {
                                    "png" -> "png"
                                    "jpg", "jpeg" -> "jpg"
                                    "gif" -> "gif"
                                    "webp" -> "webp"
                                    else -> throw Exception("Goodbye image is unknown image subtype ${bodyType.subtype}")
                                }

                                tmpImg.outputStream().use { fileOut ->
                                    body.byteStream().use { fileIn ->
                                        fileIn.copyTo(fileOut)
                                    }
                                }

                                addFile(tmpImg.toPath()) {
                                    filename = "goodbye-image.$imgFileExt"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Find the [CommunityGreetingSettings] for the given [communityId].
     *
     * @param communityId The ID of the community to find settings for.
     * @return The [CommunityGreetingSettings] for the given [communityId], or null if none exist.
     */
    suspend fun findSettings(communityId: PlatformId) =
        newSuspendedTransaction {
            CommunityGreetingSettings.findByCommunityId(communityId)
        }
}