package dev.lizainslie.pitohui.core.commands

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel
import dev.lizainslie.pitohui.core.platforms.PlatformChannel

interface ArgumentType<out T> {
    suspend fun parse(value: Any, context: CommandContext): T

    suspend fun tryParse(value: Any, context: CommandContext): Result<T> =
        try {
            println("Parsing value: $value on platform: ${context.platform} of type: ${value::class.simpleName} for ${this::class.simpleName}")
            Result.success(parse(value, context))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun validate(value: Any, context: CommandContext): Boolean {
        return try {
            parse(value, context)
            true
        } catch (e: Exception) {
            false
        }
    }
}

object ArgumentTypes {
    object STRING : ArgumentType<String> {
        override suspend fun parse(value: Any, context: CommandContext): String {
            return value.toString()
        }
    }

    object INT : ArgumentType<Int> {
        override suspend fun parse(value: Any, context: CommandContext): Int {
            return value.toString().toInt()
        }
    }

    object CHANNEL : ArgumentType<PlatformChannel> {
        override suspend fun parse(value: Any, context: CommandContext): PlatformChannel {
            return when (value) {
                is Channel -> PlatformChannel.DiscordChannel(value)
                is String -> {
                    val channelId =
                        if (value.startsWith("<#") && value.endsWith(">")) // this is a formatted mention
                            value.substring(2, value.length - 1).toLong()
                        else if (value.all { it.isDigit() }) // this is a raw ID
                            value.toLong()
                        else throw IllegalArgumentException("Invalid channel ID format: $value") // ohno.jpg

                    val channel = context.bot.getDiscordChannelById(channelId)
                        ?: throw IllegalArgumentException("Channel not found: $value")

                    PlatformChannel.DiscordChannel(channel)
                }
                else -> {
                    throw IllegalArgumentException("Invalid channel type: ${value::class.simpleName}")
                }
            }
        }
    }

    object ROLE : ArgumentType<ULong> { // FIXME: this is shit but will get polished
        override suspend fun parse(value: Any, context: CommandContext): ULong {
            return when (value) {
                is Role -> value.id.value
                is Snowflake -> value.value
                is String -> {
                    val roleId =
                        if (value.startsWith("<@&") && value.endsWith(">")) // this is a formatted mention
                            value.substring(2, value.length - 1).toULong()
                        else if (value.all { it.isDigit() }) // this is a raw ID
                            value.toULong()
                        else throw IllegalArgumentException("Invalid channel ID format: $value") // ohno.jpg

                    roleId
                }
                else -> throw IllegalArgumentException("I'm in danger")
            }
        }
    }
}
