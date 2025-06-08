package dev.lizainslie.pitohui.modules.system.commands

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.commands.platform.DiscordSlashCommandContext
import dev.lizainslie.pitohui.core.platforms.Platforms

val BOT_DESCRIPTION = """Pitohui is a multi-platform bot for Discord, and more coming soon. It is designed to
be modular and extensible, allowing for easy addition of new features. However,
Pito is not publicly available nor is it designed to be. It is a personal
project that you can customize and host yourself if you like, however."""

val AboutCommand = defineCommand(
    name = "info",
    description = "About the bot",
) {
    handle { context ->
        val thumbUrl = "https://btw.i-use-ar.ch/i/i3c8qlxducy87.gif"

        when (context.platform) {
            Platforms.DISCORD -> {
                when (context) {
                    is DiscordSlashCommandContext -> {
                        context.interaction.respondPublic {
                            embed {
                                title = "About Pitohui"
                                description = BOT_DESCRIPTION

                                thumbnail {
                                    url = thumbUrl
                                }

                                // when customizing this, please leave the
                                // original author and license fields intact or
                                // link to the original project in some way <3
                                field("Author", true) { "[Mey/Lizzy Ainslie](https://lizainslie.dev)" }
                                field("Source Code", true) { "[Git](https://git.lizainslie.dev/mey/pitohui)" }
                                field("License", true) { "[MIT](https://git.lizainslie.dev/mey/pitohui/-/raw/main/LICENSE)" } // todo: change this link when put on git

                                footer {
                                    text = "Pitohui v{version}, receiving on ${context.platform.displayName}."
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}