package dev.lizainslie.pitohui.platforms.discord.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.ArgumentDescriptor
import dev.lizainslie.pitohui.core.data.DeveloperOptions
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DiscordSlashCommandContext(
    bot: Bot,
    module: AbstractModule,
    platform: PlatformAdapterFactory<*, *>,

    val interaction: ChatInputCommandInteraction
) : DiscordCommandContext(bot, module, platform) {
    override suspend fun respond(text: String) {
        interaction.respondPublic {
            content = text
        }
    }

    suspend fun respond(builder: InteractionResponseCreateBuilder.() -> Unit) = interaction.respondPublic(builder)

    override suspend fun respondPrivate(text: String) {
        interaction.respondEphemeral {
            content = text
        }
    }

    suspend fun respondPrivate(builder: InteractionResponseCreateBuilder.() -> Unit) = interaction.respondEphemeral(builder)

    override suspend fun respondError(text: String) {
        interaction.respondEphemeral {
            content = text
        }
    }

    override fun <T> resolveRawArgumentValue(arg: ArgumentDescriptor<T>): T? {
        println("Context is `DiscordSlashCommandContext`")
        val opts = interaction.command.options

        println("Command options: $opts")

        val option = opts[arg.name]

        return option?.value?.let { value ->
            println("Found option `$option` with value: `$value`")
            arg.argumentType.tryParse(value, this).getOrDefault(arg.defaultValue)
        }
    }

    override val channelId: PlatformId = interaction.channelId.platform()
    override val callerId: PlatformId = interaction.user.id.platform()
    override val guildId: PlatformId? = interaction.invokedCommandGuildId?.platform()
    override val isInGuild: Boolean = interaction.invokedCommandGuildId != null
}
