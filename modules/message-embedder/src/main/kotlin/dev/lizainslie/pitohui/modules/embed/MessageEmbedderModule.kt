package dev.lizainslie.pitohui.modules.embed

import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.embed.data.tables.EmbeddedMessageTable
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.platforms.discord.Discord

@SupportPlatforms(Discord::class)
object MessageEmbedderModule : AbstractModule() {
    override val name = "message-embedder"
    override val visibility = ModuleVisibility.MODERATOR
    override val description = "Send embedded messages in communities."
    override val commands = setOf<RootCommand>()
    override val dependencies = setOf(SystemModule.name)

    override val tables = setOf(
        EmbeddedMessageTable
    )
}
