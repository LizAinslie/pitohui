package dev.lizainslie.pitohui.core.commands.argument

import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.platforms.AnyPlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.validation.ValidationResult
import dev.lizainslie.pitohui.core.validation.Validator
import dev.lizainslie.pitohui.core.validation.buildValidator
import dev.lizainslie.pitohui.core.validation.hexColor
import dev.lizainslie.pitohui.core.validation.validateValue
import org.slf4j.LoggerFactory
import java.awt.Color
import kotlin.time.Duration

interface ArgumentType<T : Any> {
    fun parse(value: Any, platform: AnyPlatformAdapter): T

    fun tryParse(value: Any, platform: AnyPlatformAdapter) =
        try {
            val log = LoggerFactory.getLogger(this.javaClass)
            log.info("Parsing value: `$value` on platform ${platform.key} of value type ${value::class.simpleName} for ArgumentType.${this::class.simpleName}")
            Result.success(parse(value, platform))
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
        override fun parse(value: Any, platform: AnyPlatformAdapter) =
            value.toString()
    }

    object COLOR : ArgumentType<Color> {
        override val preValidator =
            buildValidator<Any> {
                isType<String> {
                    hexColor()
                }
            }

        @Throws(NumberFormatException::class)
        override fun parse(value: Any, platform: AnyPlatformAdapter): Color =
            Color.decode(value.toString())
    }

    object INT : ArgumentType<Int> {
        override fun parse(value: Any, platform: AnyPlatformAdapter) =
            value.toString().toInt()
    }

    object BOOLEAN : ArgumentType<Boolean> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, platform: AnyPlatformAdapter) =
            when (value.toString().lowercase()) {
                "true", "yes", "1" -> true
                "false", "no", "0" -> false
                else -> throw IllegalArgumentException("Invalid boolean value: $value")
            }
    }

    object DURATION : ArgumentType<Duration> {
        override fun parse(value: Any, platform: AnyPlatformAdapter) =
            Duration.parse(value.toString())
    }

    object CHANNEL : ArgumentType<PlatformId> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, platform: AnyPlatformAdapter) =
            platform.channelArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${platform.displayName} does not support CHANNEL arguments.")
    }

    object ROLE : ArgumentType<PlatformId> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, platform: AnyPlatformAdapter) =
            platform.roleArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${platform.displayName} does not support ROLE arguments.")
    }

    object USER : ArgumentType<PlatformId> {
        @Throws(IllegalArgumentException::class)
        override fun parse(value: Any, platform: AnyPlatformAdapter) =
            platform.userArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${platform.displayName} does not support USER arguments.")
    }
}
