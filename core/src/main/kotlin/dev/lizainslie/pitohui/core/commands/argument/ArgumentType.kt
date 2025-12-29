package dev.lizainslie.pitohui.core.commands.argument

import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.validation.ValidationResult
import dev.lizainslie.pitohui.core.validation.Validator
import dev.lizainslie.pitohui.core.validation.buildValidators
import dev.lizainslie.pitohui.core.validation.validateValue
import dev.lizainslie.pitohui.core.validation.validators.StringValidators
import org.slf4j.LoggerFactory
import java.awt.Color
import kotlin.time.Duration

interface ArgumentType<T : Any> {
    fun parse(value: Any, context: CommandContext): T

    fun tryParse(value: Any, context: CommandContext) =
        try {
            val log = LoggerFactory.getLogger(this.javaClass)
            log.info("Parsing value: `$value` on platform ${context.platform.key} of value type ${value::class.simpleName} for ArgumentType.${this::class.simpleName}")
            Result.success(parse(value, context))
        } catch (e: Exception) {
            Result.failure(e)
        }

    val preValidator: Validator<Any>? get() = null

    fun preValidate(value: Any) =
        preValidator?.validate(value) ?: ValidationResult.Valid

    fun validate(
        value: T,
        descriptor: ArgumentDescriptor<T>,
    ) = descriptor.validator?.let { validateValue(value, it) } ?: ValidationResult.Valid
}

object ArgumentTypes {
    object STRING : ArgumentType<String> {
        override fun parse(value: Any, context: CommandContext) =
            value.toString()
    }

    object COLOR : ArgumentType<Color> {
        override val preValidator =
            buildValidators<Any> {
                isType<String> {
                }
            }

        @Throws(NumberFormatException::class)
        override fun parse(value: Any, context: CommandContext): Color =
            Color.decode(value.toString())
    }

    object INT : ArgumentType<Int> {
        override fun parse(value: Any, context: CommandContext) =
            value.toString().toInt()
    }

    object BOOLEAN : ArgumentType<Boolean> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, context: CommandContext) =
            when (value.toString().lowercase()) {
                "true", "yes", "1" -> true
                "false", "no", "0" -> false
                else -> throw IllegalArgumentException("Invalid boolean value: $value")
            }
    }

    object DURATION : ArgumentType<Duration> {
        override fun parse(value: Any, context: CommandContext) =
            Duration.parse(value.toString())
    }

    object CHANNEL : ArgumentType<PlatformId> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, context: CommandContext) =
            context.platform.channelArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support CHANNEL arguments.")

    }

    object ROLE : ArgumentType<PlatformId> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, context: CommandContext) =
            context.platform.roleArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support ROLE arguments.")
    }

    object USER : ArgumentType<PlatformId> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, context: CommandContext) =
            context.platform.userArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support USER arguments.")
    }
}
