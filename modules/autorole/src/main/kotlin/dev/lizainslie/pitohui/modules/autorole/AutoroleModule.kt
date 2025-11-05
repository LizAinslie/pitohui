package dev.lizainslie.pitohui.modules.autorole

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.guild.MemberJoinEvent
import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.autorole.commands.AutoroleCommand
import dev.lizainslie.pitohui.modules.autorole.data.AutoroleSettings
import dev.lizainslie.pitohui.modules.autorole.data.AutoroleSettingsTable
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@SupportPlatforms(Discord::class)
class AutoroleModule : AbstractModule() {
    override val name = "autorole"
    override val description = "Assign each new member a role upon joining the community"
    override val visibility = ModuleVisibility.MODERATOR

    override val commands = setOf(
        AutoroleCommand
    )

    override val tables = setOf(
        AutoroleSettingsTable
    )

    override fun onLoad() {
        super.onLoad()

        Discord.get().addEventListener<MemberJoinEvent> {
            val communityId = guildId.platform

            val autoroleSettings = newSuspendedTransaction {
                AutoroleSettings.getAutoroleSettings(communityId)
            } ?: return@addEventListener

            if (member.isBot)
                autoroleSettings.botRoleId?.let { botRoleId ->
                    member.addRole(Snowflake(botRoleId), "pitohui: autorole")
                }
            else
                autoroleSettings.memberRoleId?.let { memberRoleId ->
                    member.addRole(Snowflake(memberRoleId), "pitohui: autorole")
                }
        }
    }
}