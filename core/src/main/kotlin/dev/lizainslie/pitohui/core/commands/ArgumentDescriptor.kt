package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.commands.platform.DiscordSlashCommandContext

class ArgumentDescriptor<T>(
    val name: String,
    val description: String,
    val argumentType: ArgumentType<T>,
    val defaultValue: T? = null,
    val required: Boolean = false,
    val autoComplete: () -> List<String> = { emptyList() }
) {
    suspend fun resolve(commandContext: CommandContext): T? {
        println("Resolving argument `$name` with type `${argumentType::class.simpleName}`")
        val value = when (commandContext) {
            is DiscordSlashCommandContext -> {
                println("Context is `DiscordSlashCommandContext`")
                val args = commandContext.interaction.command.options

                println("Command options: $args")

                val option = args[name]

                option?.value?.let { value ->
                    println("Found option `$option` with value: `$value`")
                    argumentType.tryParse(value, commandContext).getOrDefault(defaultValue)
                }
            }
            else -> null
        }

        println("Resolved value: $value")

        if (value == null && required) {
            commandContext.respond("Option `$name` is required and no default value is provided")
            throw IllegalArgumentException("Option  `$name` is required and no default value is provided")
        }

        return value
    }
}