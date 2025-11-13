package dev.lizainslie.pitohui.modules.autorole

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.guild.MemberJoinEvent
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.autorole.commands.AutoroleAdminCommand
import dev.lizainslie.pitohui.modules.autorole.data.AutoroleSettings
import dev.lizainslie.pitohui.modules.autorole.data.AutoroleSettingsTable
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@SupportPlatforms(Discord::class)
object AutoroleModule : AbstractModule(
    "autorole",
    description = "Assign each new member a role upon joining the community",
    visibility = ModuleVisibility.MODERATOR,
    commands = setOf(
        AutoroleAdminCommand
    ),
    tables = setOf(
        AutoroleSettingsTable
    )
) {
    override fun onInit(bot: Bot) {
        super.onInit(bot)

        Discord.get().addEventListener<MemberJoinEvent> {
            val communityId = guildId.platform

            println("new member joined $communityId. bot? ${member.isBot}")

            val autoroleSettings = newSuspendedTransaction {
                AutoroleSettings.getAutoroleSettings(communityId)
            } ?: return@addEventListener

            if (member.isBot)
                autoroleSettings.botRoleId?.let { botRoleId ->
                    println("assigning role $botRoleId to user ${member.id.platform} in community $communityId")
                    member.addRole(Snowflake(botRoleId), "pitohui: autorole")
                }
            else
                autoroleSettings.memberRoleId?.let { memberRoleId ->
                    println("assigning role $memberRoleId to user ${member.id.platform} in community $communityId")
                    member.addRole(Snowflake(memberRoleId), "pitohui: autorole")
                }
        }
    }
}