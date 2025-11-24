package dev.lizainslie.pitohui.core.commands

import org.slf4j.LoggerFactory

class ArgumentDescriptor<T>(
    val name: String,
    val description: String,
    val argumentType: ArgumentType<T>,
    val defaultValue: T? = null,
    val required: Boolean = false,
    val autoComplete: () -> List<String> = { emptyList() }
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    suspend fun resolve(commandContext: CommandContext): T? {
        log.debug("Resolving argument `$name` with type `${argumentType::class.simpleName}`")
        val value = commandContext.resolveRawArgumentValue(this)

        log.debug("Resolved value: `$value`")

        if (value == null && required) {
            commandContext.respondError("Option `$name` is required and no default value is provided")
            throw IllegalArgumentException("Option `$name` is required and no default value is provided")
        }

        return value
    }
}
