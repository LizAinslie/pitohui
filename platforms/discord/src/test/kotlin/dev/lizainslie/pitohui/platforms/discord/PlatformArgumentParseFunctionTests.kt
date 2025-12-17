package dev.lizainslie.pitohui.platforms.discord

import dev.kord.common.entity.Snowflake
import kotlin.test.Test

class PlatformArgumentParseFunctionTests {
    @Test
    fun `user argument parser should parse raw mentions`() {
        val userId = "543542278967394322"
        val rawMention = "<@$userId>"

        val parsedId = Discord.userArgumentParser.parse(rawMention)

        println("$parsedId vs $userId")
        assert(parsedId.id == userId)
    }

    @Test
    fun `user argument parser should parse snowflakes`() {
        val userId = "543542278967394322"
        val snowflake = Snowflake(userId)

        val parsedId = Discord.userArgumentParser.parse(snowflake)

        println("$parsedId vs $userId")
        assert(parsedId.id == userId)
    }

    @Test
    fun `channel argument parser should parse raw mentions`() {
        val channelId = "799896283803811911"
        val rawMention = "<#$channelId>"

        val parsedId = Discord.channelArgumentParser.parse(rawMention)

        println("$parsedId vs $channelId")
        assert(parsedId.id == channelId)
    }

    @Test
    fun `channel argument parser should parse snowflakes`() {
        val channelId = "799896283803811911"
        val snowflake = Snowflake(channelId)

        val parsedId = Discord.channelArgumentParser.parse(snowflake)
        println("$parsedId vs $channelId")
        assert(parsedId.id == channelId)
    }

    @Test
    fun `role argument parser should parse raw mentions`() {
        val roleId = "799885129970483231"
        val rawMention = "<@&$roleId>"

        val parsedId = Discord.roleArgumentParser.parse(rawMention)

        println("$parsedId vs $roleId")
        assert(parsedId.id == roleId)
    }

    @Test
    fun `role argument parser should parse snowflakes`() {
        val roleId = "799885129970483231"
        val snowflake = Snowflake(roleId)

        val parsedId = Discord.roleArgumentParser.parse(snowflake)
        println("$parsedId vs $roleId")
        assert(parsedId.id == roleId)
    }
}