package dev.lizainslie.pitohui

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import dev.lizainslie.moeka.core.Bot
import dev.lizainslie.moeka.platforms.discord.Discord

import dev.lizainslie.pitohui.modules.admin.AdminModule
import dev.lizainslie.pitohui.modules.system.SystemModule

class PitohuiBot : SuspendingCliktCommand() {
    override suspend fun run() {
        val bot = Bot(SystemModule, AdminModule)

        bot.enablePlatforms(Discord)
        bot.loadModules()

        bot.init()
        bot.start()
    }
}

suspend fun main(args: Array<String>) = PitohuiBot().main(args)
