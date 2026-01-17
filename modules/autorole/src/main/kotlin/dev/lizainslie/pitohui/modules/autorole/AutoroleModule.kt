package dev.lizainslie.pitohui.modules.autorole

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.guild.MemberJoinEvent
import dev.lizainslie.moeka.core.Bot
import dev.lizainslie.moeka.core.modules.AbstractModule
import dev.lizainslie.moeka.core.modules.ModuleVisibility
import dev.lizainslie.moeka.core.platforms.SupportPlatforms
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.moeka.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.modules.autorole.commands.AutoroleAdminCommand
import dev.lizainslie.pitohui.modules.autorole.data.entities.AutoroleSettings
import dev.lizainslie.pitohui.modules.autorole.data.tables.AutoroleSettingsTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@SupportPlatforms(Discord::class)
object AutoroleModule : AbstractModule(
    "autorole",
    description = "Assign each new member a role upon joining the community",
    visibility = ModuleVisibility.MODERATOR,
    commands =
        setOf(
            AutoroleAdminCommand,
        ),
    tables =
        setOf(
            AutoroleSettingsTable,
        ),
) {
    override fun onInit(bot: Bot) {
        super.onInit(bot)

        Discord.addEventListener<MemberJoinEvent> {
            val communityId = guildId.platform

            log.info("new member joined $communityId. bot? ${member.isBot}")

            val autoroleSettings =
                newSuspendedTransaction {
                    AutoroleSettings.getAutoroleSettings(communityId)
                } ?: return@addEventListener

            if (member.isBot) {
                autoroleSettings.botRoleId?.let { botRoleId ->
                    log.info("assigning role $botRoleId to user ${member.id.platform} in community $communityId")
                    member.addRole(Snowflake(botRoleId), "pitohui: autorole")
                }
            } else {
                autoroleSettings.memberRoleId?.let { memberRoleId ->
                    log.info("assigning role $memberRoleId to user ${member.id.platform} in community $communityId")
                    member.addRole(Snowflake(memberRoleId), "pitohui: autorole")
                }
            }
        }
    }
}
