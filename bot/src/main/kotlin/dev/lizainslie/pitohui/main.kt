package dev.lizainslie.pitohui

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import dev.lizainslie.pitohui.core.config.BotConfig
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.modules.admin.AdminModule
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.modules.vcnotify.VcNotifyModule

class PitohuiBot : SuspendingCliktCommand() {
    val config by option(help = "Path to the config file").file(mustExist = true, canBeDir = false).required()

    override suspend fun run() {
        val config = BotConfig.load(config)
        val bot = Bot(config)

        bot.loadModule(SystemModule)
        bot.loadModule(AdminModule)
        bot.loadModule(VcNotifyModule)

        bot.init()
        bot.login()
    }
}

suspend fun main(args: Array<String>) = PitohuiBot().main(args)