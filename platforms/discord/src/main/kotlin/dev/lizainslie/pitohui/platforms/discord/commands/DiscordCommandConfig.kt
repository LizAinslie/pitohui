package dev.lizainslie.pitohui.platforms.discord.commands

import dev.kord.common.entity.Permissions
import dev.lizainslie.pitohui.core.commands.PlatformCommandConfig

class DiscordCommandConfig : PlatformCommandConfig {
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean = true
    var nsfw: Boolean = false
}