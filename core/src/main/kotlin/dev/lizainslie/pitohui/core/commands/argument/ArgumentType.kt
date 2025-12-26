package dev.lizainslie.pitohui.core.commands.argument

import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.platforms.PlatformId
import org.slf4j.LoggerFactory
import kotlin.time.Duration

interface ArgumentType<out T> {
    fun parse(value: Any, context: CommandContext): T

    fun tryParse(value: Any, context: CommandContext) =
        try {
            val log = LoggerFactory.getLogger(this.javaClass)
            log.info("Parsing value: `$value` on platform ${context.platform.key} of value type ${value::class.simpleName} for ArgumentType.${this::class.simpleName}")
            Result.success(parse(value, context))
        } catch (e: Exception) {
            Result.failure(e)
        }

    fun validate(value: Any, context: CommandContext) =
        try {
            parse(value, context)
            true
        } catch (e: Exception) {
            false
        }
}

object ArgumentTypes {
    object STRING : ArgumentType<String> {
        override fun parse(value: Any, context: CommandContext) =
            value.toString()
    }

    object INT : ArgumentType<Int> {
        override fun parse(value: Any, context: CommandContext) =
            value.toString().toInt()
    }

    object BOOLEAN : ArgumentType<Boolean> {
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
        override fun parse(value: Any, context: CommandContext): PlatformId =
            context.platform.channelArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support CHANNEL arguments.")

    }

    object ROLE : ArgumentType<PlatformId> {
        override fun parse(value: Any, context: CommandContext): PlatformId =
            context.platform.roleArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support ROLE arguments.")
    }

    object USER : ArgumentType<PlatformId> {
        override fun parse(value: Any, context: CommandContext): PlatformId =
            context.platform.userArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support USER arguments.")
    }
}
