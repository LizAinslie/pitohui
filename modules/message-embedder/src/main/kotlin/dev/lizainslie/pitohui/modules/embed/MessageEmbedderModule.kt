package dev.lizainslie.pitohui.modules.embed

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.embed.data.tables.EmbeddedMessageTable
import dev.lizainslie.pitohui.platforms.discord.Discord

@SupportPlatforms(Discord::class)
object MessageEmbedderModule : AbstractModule(
    "message-embedder",
    description = "Send embedded messages in communities.",
    visibility = ModuleVisibility.MODERATOR,
    tables = setOf(
        EmbeddedMessageTable
    ),
)
