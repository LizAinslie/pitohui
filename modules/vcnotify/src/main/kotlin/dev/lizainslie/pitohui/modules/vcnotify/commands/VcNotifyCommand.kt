package dev.lizainslie.pitohui.modules.vcnotify.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.commands.platform.DiscordSlashCommandContext
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.modules.vcnotify.VcNotifyModule
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifyRecord
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifySettings
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

    handle { context ->
        if (context !is DiscordSlashCommandContext) {
            context.respond("This command is currently only available on Discord.")
            return@handle
        }

        if (context.interaction !is GuildChatInputCommandInteraction) {
            context.respond("This command can only be used in a server.")
            return@handle
        }

        val guildInteraction = (context.interaction as GuildChatInputCommandInteraction)
        val platformId = PlatformId.fromSnowflake(guildInteraction.guildId)
        val lastUsedRecord = VcNotifyModule.communitiesLastUsed[platformId]

        val settings = transaction{ VcNotifySettings.getSettings(platformId) } ?: run {
            context.respond("VcNotify is not configured for this community. Please contact an admin.")
            return@handle
        }

        val voiceState = guildInteraction.user.getVoiceStateOrNull()

        if (voiceState == null) {
            context.respondNotInChannel()
            return@handle
        }

        if (voiceState.channelId == null) {
            context.respondNotInChannel()
            return@handle
        }

        val channel = guildInteraction.guild.getChannelOrNull(voiceState.channelId!!) ?: run {
            context.respondNotInChannel()
            return@handle
        }

        suspend fun notify() {
            VcNotifyModule.communitiesLastUsed[platformId] = VcNotifyRecord(
                time = Clock.System.now(),
                user = guildInteraction.user.mention
            )

            GlobalScope.launch {
                delay(settings.cooldown)

                VcNotifyModule.communitiesLastUsed.remove(platformId)
            }

            val role = settings.roleId?.let { id ->
                guildInteraction.guild.roles.firstOrNull { role -> role.id.value == id }
            } ?: run {
                context.respond("The role set to notify was not found or has been removed. Please contact an admin.")
                return
            }

            context.interaction.respondPublic {
                content = settings.messageFormat
                    .replace("{role}", role.mention)
                    .replace("{user}", guildInteraction.user.mention)
                    .replace("{channelLink}", "https://discord.com/channels/${guildInteraction.guildId.value}/${voiceState.channelId!!.value}")
                    .replace("{channel}", channel.mention)
            }
        }

        if (lastUsedRecord != null) {
            val timeSinceLastUse = Clock.System.now() - lastUsedRecord.time
            val remainingTime = settings.cooldown - timeSinceLastUse

            if (remainingTime > 0.seconds) {
                context.interaction.respondEphemeral {
                    content = "You have already notified members in this voice channel recently.\nYou must wait ${formatDuration(remainingTime)} before notifying again.\n-# Last used by ${lastUsedRecord.user}."
                }

                return@handle
            } else notify()
        } else notify()
    }
}