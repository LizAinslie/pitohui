package dev.lizainslie.pitohui.core

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.lizainslie.pitohui.core.commands.Commands
import dev.lizainslie.pitohui.core.config.BotConfig
import dev.lizainslie.pitohui.core.data.DbContext
import dev.lizainslie.pitohui.core.data.ModuleSwitchTable
import dev.lizainslie.pitohui.core.modules.AbstractModule

class Bot(
    val config: BotConfig,
) {
    val db = DbContext(this).also {
        it.tables += ModuleSwitchTable
    }

    lateinit var kord: Kord
    lateinit var commands: Commands

    val modules = mutableListOf<AbstractModule>()



    fun loadModule(module: AbstractModule) {
        if (modules.any { it.name == module.name }) {
            throw IllegalArgumentException("Module with name ${module.name} already loaded")
        }

        db.tables += module.tables

        // check dependencies
        for (dependency in module.dependencies) {
            if (modules.none { it.name == dependency }) {
                throw IllegalArgumentException("Module ${module.name} depends on module $dependency, which is not loaded")
            }
        }

        modules += module
        module.onLoad()
    }

    suspend fun init() {
        kord = Kord(config.token)
        commands = Commands(this)
        commands.initialize()

        for (module in modules) {
            module.onInit(this)

            for (command in module.commands) {
                commands.registerCommand(command, module)
            }
        }
    }

    @OptIn(PrivilegedIntent::class)
    suspend fun login() {
        kord.login {
            intents = Intents.ALL
        }
    }

    suspend fun getDiscordChannelById(id: Snowflake) = kord.getChannel(id)
    suspend fun getDiscordChannelById(id: Long) = kord.getChannel(Snowflake(id))
}