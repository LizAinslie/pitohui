package dev.lizainslie.pitohui.modules.vcdisconnect

import dev.kord.core.behavior.edit
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.vcdisconnect.commands.VcDisconnectCommand
import dev.lizainslie.pitohui.modules.vcdisconnect.data.DisconnectTimer
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import dev.lizainslie.pitohui.util.task.startRepeatingTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

@SupportPlatforms(Discord::class)
object VcDisconnectModule : AbstractModule(
    "vcdisconnect",
    description = "Allows users to set a timer to be disconnected from a voice channel after a specified duration.",
    commands = setOf(
        VcDisconnectCommand
    ),
) {
    val communitiesDisconnectTimers = mutableMapOf<PlatformId, MutableList<DisconnectTimer>>()

    var disconnectTask: Job? = null

    override fun onInit(bot: Bot) {
        super.onInit(bot)

        disconnectTask = startRepeatingTask(15.seconds, Dispatchers.IO, ::disconnectUsers)

        Discord.addEventListener<VoiceStateUpdateEvent> {
            val oldState = old ?: return@addEventListener
            val newState = state

            if (newState.channelId == null) {
                // User disconnected from voice channel, clear any existing timers
                clearDisconnectTimer(
                    communityId = newState.guildId.platform,
                    userId = newState.userId.platform
                )
            } else if (oldState.channelId != newState.channelId) {
                // User switched voice channels, update the timer to the new channel
                changeChannelInDisconnectTimer(
                    communityId = newState.guildId.platform,
                    userId = newState.userId.platform,
                    newChannelId = newState.channelId!!.platform
                )
            }
        }
    }

    override fun onUnload() {
        super.onUnload()
        disconnectTask?.cancel()
    }

    suspend fun disconnectUsers() {
        val now = Clock.System.now()
        for ((communityId, timers) in communitiesDisconnectTimers) {
            val toDisconnect = timers.filter { it.disconnectAt <= now }

            if (toDisconnect.isNotEmpty()) log.info("Disconnecting ${toDisconnect.size} users in community $communityId")

            val guild = Discord.getGuildById(communityId) ?: return
            for (timer in toDisconnect) {
                val member = guild.getMemberOrNull(timer.user.snowflake) ?: continue
                val channel = guild.getChannelOrNull(timer.channel.snowflake) ?: continue

                if (member.getVoiceState().channelId == channel.id) {
                    member.edit {
                        voiceChannelId = null
                    }

                    member
                        .asUser()
                        .getDmChannel()
                        .createMessage("You have been disconnected from <#${channel.id}> as your timer has expired.")
                }
            }

            timers.removeAll(toDisconnect)
        }
    }

    fun changeChannelInDisconnectTimer(communityId: PlatformId, userId: PlatformId, newChannelId: PlatformId) {
        val timers = communitiesDisconnectTimers[communityId] ?: return
        val timerIndex = timers.indexOfFirst { it.user == userId }
        if (timerIndex == -1) return
        communitiesDisconnectTimers[communityId]?.get(timerIndex)?.channel = newChannelId
    }

    fun addDisconnectTimer(communityId: PlatformId, timer: DisconnectTimer) {
        communitiesDisconnectTimers.getOrPut(communityId) { mutableListOf() } += timer
    }

    fun clearDisconnectTimer(communityId: PlatformId, userId: PlatformId) {
        communitiesDisconnectTimers[communityId]?.removeIf { it.user == userId }
    }
}
