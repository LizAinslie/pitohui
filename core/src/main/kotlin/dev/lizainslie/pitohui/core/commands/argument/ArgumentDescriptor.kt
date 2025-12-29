package dev.lizainslie.pitohui.core.commands.argument

import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.validation.Validator
import org.slf4j.LoggerFactory

class ArgumentDescriptor<T : Any>(
    val name: String,
    val description: String,
    val argumentType: ArgumentType<T>,
    val defaultValue: T? = null,
    val required: Boolean = false,
    val validator: Validator<T>? = null,
    val autoComplete: () -> List<String> = { emptyList() }
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    suspend fun resolve(commandContext: CommandContext): T? {
        log.debug("Resolving argument `$name` with type `${argumentType::class.simpleName}`")

        val value = commandContext.resolveRawArgumentValue(this)
        log.debug("Resolved value: `{}`", value)

        if (value == null && required) {
            val error = "Option `$name` is required and no default value is provided"
            commandContext.respondError(error)
            throw IllegalArgumentException(error)
        }

//        value?.let {
//            val validationResult = argumentType.validate(it, this)
//            if (!validationResult.valid) {
//                val errorMessage = buildString {
//                    appendLine("Error${if (validationResult.errors.size != 1) "s" else ""} while validating command argument")
//
//                    for (error in validationResult.errors) {
//                        appendLine(error)
//                    }
//                }
//            }
//        }

        return value
    }
}
