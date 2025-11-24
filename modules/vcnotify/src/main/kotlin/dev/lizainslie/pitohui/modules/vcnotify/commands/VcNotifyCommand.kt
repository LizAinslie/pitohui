package dev.lizainslie.pitohui.modules.vcnotify.commands

import dev.kord.common.entity.Snowflake
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.modules.vcnotify.VcNotifyModule
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifyRecord
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifySettings
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordCommandContext
import dev.lizainslie.pitohui.platforms.discord.commands.enforceDiscord
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.util.time.formatDuration
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.seconds

internal suspend fun CommandContext.respondNotInChannel() {
    respondError("You must be in a voice channel to use this command.")
}

@OptIn(DelicateCoroutinesApi::class) // DelicateCoroutinesApi is used here because we are using GlobalScope.launch, which is generally discouraged
val VcNotifyCommand = defineCommand("vcnotify", "Notify other members you are in a voice channel") {
    platforms(Discord)

    handle {
        enforceDiscord<DiscordCommandContext> {
            enforceGuild { guild ->
                val communityId = guild.id.platform
                val member = getMember()!!
                val lastUsedRecord = VcNotifyModule.communitiesLastUsed[communityId]

                val settings = newSuspendedTransaction { VcNotifySettings.getSettings(communityId) } ?: run {
                    respondError("VcNotify is not configured for this community. Please contact an admin.")
                    return@enforceGuild
                }

                val voiceState = getMember()!!.getVoiceStateOrNull()

                if (voiceState == null) {
                    respondNotInChannel()
                    return@enforceGuild
                }

                if (voiceState.channelId == null) {
                    respondNotInChannel()
                    return@enforceGuild
                }

                val channel = guild.getChannelOrNull(voiceState.channelId!!) ?: run {
                    respondNotInChannel()
                    return@enforceGuild
                }

                suspend fun notify() {
                    VcNotifyModule.communitiesLastUsed[communityId] = VcNotifyRecord(
                        time = Clock.System.now(),
                        user = callerId
                    )

                    GlobalScope.launch {
                        delay(settings.cooldown)

                        VcNotifyModule.communitiesLastUsed.remove(communityId)
                    }

                    val role = settings.roleId?.let { id ->
                        guild.getRoleOrNull(Snowflake(id))
                    } ?: run {
                        respondError("The role set to notify was not found or has been removed. Please contact an admin.")
                        return
                    }

                    respond(settings.messageFormat
                        .replace("{role}", role.mention)
                        .replace("{user}", member.mention)
                        .replace(
                            "{channelLink}",
                            "https://discord.com/channels/${communityId.id}/${voiceState.channelId!!.value}"
                        )
                        .replace("{channel}", channel.mention)
                    )
                }

                if (lastUsedRecord != null) {
                    val timeSinceLastUse = Clock.System.now() - lastUsedRecord.time
                    val remainingTime = settings.cooldown - timeSinceLastUse

                    if (remainingTime > 0.seconds) {
                        val lastUser = Discord.getUserById(lastUsedRecord.user)

                        respondError(
                            "You have already notified members in this voice channel recently.\nYou must wait ${
                                formatDuration(remainingTime)
                            } before notifying again.\n${lastUser?.let { "-# Last used by ${it.mention}." }}"
                        )

                        return@enforceGuild
                    } else notify()
                } else notify()
            }
        }
    }
}
