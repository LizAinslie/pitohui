package dev.lizainslie.pitohui.modules.vcnotify

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.vcnotify.commands.VcNotifyAdminCommand
import dev.lizainslie.pitohui.modules.vcnotify.commands.VcNotifyCommand
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifyRecord
import dev.lizainslie.pitohui.modules.vcnotify.data.tables.VcNotifySettingsTable
import dev.lizainslie.pitohui.platforms.discord.Discord

@SupportPlatforms(Discord::class)
object VcNotifyModule : AbstractModule(
    "vcnotify",
    description = "Allows users to notify others when they join a voice channel, with a cooldown to prevent spam.",
    commands = setOf(
        VcNotifyCommand,
        VcNotifyAdminCommand
    ),
    tables = setOf(
        VcNotifySettingsTable,
    ),
) {
    val communitiesLastUsed = mutableMapOf<PlatformId, VcNotifyRecord>()
}
