package dev.lizainslie.pitohui.modules.vcnotify.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.message.allowedMentions
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.modules.vcnotify.VcNotifyModule
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifyRecord
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifySettings
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordSlashCommandContext
import dev.lizainslie.pitohui.platforms.discord.extensions.DISCORD
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.util.time.formatDuration
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.seconds

internal suspend fun CommandContext.respondNotInChannel() {
    respond("You must be in a voice channel to use this command.")
}

@OptIn(DelicateCoroutinesApi::class) // DelicateCoroutinesApi is used here because we are using GlobalScope.launch, which is generally discouraged
val VcNotifyCommand = defineCommand("vcnotify", "Notify other members you are in a voice channel") {
    platforms(Platforms.DISCORD)

    handle {
        if (this !is DiscordSlashCommandContext) {
            respond("This command is currently only available on Discord.")
            return@handle
        }

        if (interaction !is GuildChatInputCommandInteraction) {
            respond("This command can only be used in a server.")
            return@handle
        }

        val guildInteraction = (interaction as GuildChatInputCommandInteraction)
        val communityId = guildInteraction.guildId.platform()
        val lastUsedRecord = VcNotifyModule.communitiesLastUsed[communityId]

        val settings = transaction { VcNotifySettings.getSettings(communityId) } ?: run {
            respond("VcNotify is not configured for this community. Please contact an admin.")
            return@handle
        }

        val voiceState = guildInteraction.user.getVoiceStateOrNull()

        if (voiceState == null) {
            respondNotInChannel()
            return@handle
        }

        if (voiceState.channelId == null) {
            respondNotInChannel()
            return@handle
        }

        val channel = guildInteraction.guild.getChannelOrNull(voiceState.channelId!!) ?: run {
            respondNotInChannel()
            return@handle
        }

        suspend fun notify() {
            VcNotifyModule.communitiesLastUsed[communityId] = VcNotifyRecord(
                time = Clock.System.now(),
                user = guildInteraction.user.mention
            )

            GlobalScope.launch {
                delay(settings.cooldown)

                VcNotifyModule.communitiesLastUsed.remove(communityId)
            }

            val role = settings.roleId?.let { id ->
                guildInteraction.guild.roles.firstOrNull { role -> role.id.value == id.toULong() }
            } ?: run {
                respond("The role set to notify was not found or has been removed. Please contact an admin.")
                return
            }

            interaction.respondPublic {
                content = settings.messageFormat
                    .replace("{role}", role.mention)
                    .replace("{user}", guildInteraction.user.mention)
                    .replace(
                        "{channelLink}",
                        "https://discord.com/channels/${guildInteraction.guildId.value}/${voiceState.channelId!!.value}"
                    )
                    .replace("{channel}", channel.mention)

                allowedMentions {
                    roles += role.id
                    users += guildInteraction.user.id
                }
            }
        }

        if (lastUsedRecord != null) {
            val timeSinceLastUse = Clock.System.now() - lastUsedRecord.time
            val remainingTime = settings.cooldown - timeSinceLastUse

            if (remainingTime > 0.seconds) {
                interaction.respondEphemeral {
                    content = "You have already notified members in this voice channel recently.\nYou must wait ${
                        formatDuration(remainingTime)
                    } before notifying again.\n-# Last used by ${lastUsedRecord.user}."
                }

                return@handle
            } else notify()
        } else notify()
    }
}
