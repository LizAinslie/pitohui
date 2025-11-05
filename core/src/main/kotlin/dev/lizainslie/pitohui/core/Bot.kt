package dev.lizainslie.pitohui.core

import dev.lizainslie.pitohui.core.commands.Commands
import dev.lizainslie.pitohui.core.config.BotConfig
import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.data.DbContext
import dev.lizainslie.pitohui.core.data.DeveloperOptionsTable
import dev.lizainslie.pitohui.core.data.ModuleSwitchTable
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import org.jetbrains.exposed.sql.Database
import kotlin.system.exitProcess

class Bot {
    val config: BotConfig

    init {
        Configs.generateBaseStructure()

        config = BotConfig.load()

        if (!config.validate()) {
            println("Main bot config invalid at ${Configs.getBotConfigFile().absolutePath}. Exiting.")
            exitProcess(1)
        }

        Database.connect(
            url = config.db.url,
            driver = "org.postgresql.Driver",
            user = config.db.user,
            password = config.db.password,
        )

        DbContext.tables += ModuleSwitchTable
        DbContext.tables += DeveloperOptionsTable
        DbContext.migrate()
    }

    lateinit var commands: Commands

    val modules = mutableListOf<AbstractModule>()
    val platformAdapters = mutableListOf<PlatformAdapterFactory<*, *>>()

    fun enablePlatforms(vararg platforms: PlatformAdapterFactory<*, *>) {
        platforms.forEach { adapter ->
            if (platformAdapters.none { it.key == adapter.key }) platformAdapters.add(adapter)
        }
    }

    suspend fun eachPlatform(block: suspend (platformAdapter: PlatformAdapter<*>) -> Unit) {
        for (platform in platformAdapters) {
            block(platform.get())
        }
    }

    fun loadModule(module: AbstractModule) {
        if (modules.any { it.name == module.name }) {
            throw IllegalArgumentException("Module with name ${module.name} already loaded")
        }

        DbContext.tables += module.tables

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
        commands = Commands(this)

        platformAdapters.forEach {
            it.load()
        }

        eachPlatform {
            it.initialize(this)
        }

        for (module in modules) {
            module.onInit(this)

            for (command in module.commands) {
                commands.registerCommand(command, module)
            }
        }

        DbContext.migrate()
    }

    suspend fun start() {
        eachPlatform {
            it.start(this)
        }
    }
}
