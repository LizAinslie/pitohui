package dev.lizainslie.pitohui.modules.embed

import dev.lizainslie.moeka.core.modules.AbstractModule
import dev.lizainslie.moeka.core.modules.ModuleVisibility
import dev.lizainslie.moeka.core.platforms.SupportPlatforms
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.pitohui.modules.embed.data.tables.EmbeddedMessageTable

@SupportPlatforms(Discord::class)
object MessageEmbedderModule : AbstractModule(
    "message-embedder",
    description = "Send embedded messages in communities.",
    visibility = ModuleVisibility.MODERATOR,
    tables =
        setOf(
            EmbeddedMessageTable,
        ),
)
