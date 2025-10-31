package dev.lizainslie.pitohui.modules.vcnotify

import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.modules.vcnotify.commands.VcNotifyAdminCommand
import dev.lizainslie.pitohui.modules.vcnotify.commands.VcNotifyCommand
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifyRecord
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifySettingsTable
import dev.lizainslie.pitohui.platforms.discord.Discord
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Table

@SupportPlatforms(Discord::class)
object VcNotifyModule : AbstractModule() {
    override val optional = true
    override val name = "vcnotify"
    override val description = "Allows users to notify others when they join a voice channel, with a cooldown to prevent spam."
    override val visibility = ModuleVisibility.PUBLIC

    override val commands = setOf(
        VcNotifyCommand,
        VcNotifyAdminCommand
    )

    override val tables = setOf<Table>(
        VcNotifySettingsTable,
    )

    override val dependencies = setOf(
        SystemModule.name
    )

    val communitiesLastUsed = mutableMapOf<PlatformId, VcNotifyRecord>()
}
