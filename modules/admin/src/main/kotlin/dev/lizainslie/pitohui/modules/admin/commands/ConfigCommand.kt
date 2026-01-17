package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.moeka.core.commands.argument.ArgumentTypes
import dev.lizainslie.moeka.core.commands.defineCommand
import dev.lizainslie.moeka.core.config.Configs
import dev.lizainslie.moeka.platforms.discord.Discord

val ConfigCommand =
    defineCommand("config", "Manage loaded configurations") {
        platform(Discord)

        subCommand("reload", "Reload a configuration") {
            val configKey =
                argument("key", "The key of the configuration", ArgumentTypes.STRING) {
                    required = true
                }

            handle {
                val key by configKey.require()

                val config = Configs.loadedConfigs.find { it.key == key }

                if (config == null) {
                    respondError("Configuration with key `$key` not found")
                    return@handle
                }

                config.load()

                respond(
                    """Loaded configuration `${config.key}`, new value:
                |```json
                |${config.jsonValue}
                |```
                    """.trimMargin(),
                )
            }
        }

        subCommand("list", "List all configurations") {
            handle {
            }
        }

        subCommand("peek", "View the contents of a config") {
            val configKey =
                argument("key", "The identifier of the configuration", ArgumentTypes.STRING) {
                    required = true
                }

            handle {
                val key by configKey.require()

                val config = Configs.loadedConfigs.find { it.key == key }

                if (config == null) {
                    respondError("Configuration with key `$key` not found")
                    return@handle
                }

                respond(
                    """The current value of config `${config.key}` is:
                |```json
                |${config.jsonValue}
                |```
                    """.trimMargin(),
                )
            }
        }
    }
