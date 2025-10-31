package dev.lizainslie.pitohui.core.commands

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
        val value = commandContext.resolveRawArgumentValue(this)

        println("Resolved value: $value")

        if (value == null && required) {
            commandContext.respondError("Option `$name` is required and no default value is provided")
            throw IllegalArgumentException("Option `$name` is required and no default value is provided")
        }

        return value
    }
}
