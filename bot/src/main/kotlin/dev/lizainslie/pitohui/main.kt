package dev.lizainslie.pitohui

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.modules.admin.AdminModule
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.platforms.discord.Discord

class PitohuiBot : SuspendingCliktCommand() {
    override suspend fun run() {
        val bot = Bot(SystemModule, AdminModule)

        bot.enablePlatforms(Discord)

        bot.init()
        bot.start()
    }
}

suspend fun main(args: Array<String>) = PitohuiBot().main(args)
