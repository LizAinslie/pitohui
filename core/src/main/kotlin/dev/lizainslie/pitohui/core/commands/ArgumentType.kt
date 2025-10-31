package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.platforms.PlatformId

interface ArgumentType<out T> {
    fun parse(value: Any, context: CommandContext): T

    fun tryParse(value: Any, context: CommandContext) =
        try {
            println("Parsing value: $value on platform: ${context.platform} of type: ${value::class.simpleName} for ${this::class.simpleName}")
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

    object CHANNEL : ArgumentType<PlatformId> {
        override fun parse(value: Any, context: CommandContext): PlatformId =
            context.platform.get().channelArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support CHANNEL arguments.")

    }

    object ROLE : ArgumentType<PlatformId> { // FIXME: this is shit but will get polished
        override fun parse(value: Any, context: CommandContext): PlatformId =
            context.platform.get().roleArgumentParser?.parse(value) ?:
            throw IllegalArgumentException("Platform ${context.platform.displayName} does not support ROLE arguments.")
    }
}
