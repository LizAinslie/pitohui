package dev.lizainslie.pitohui.core

import dev.lizainslie.pitohui.core.commands.Commands
import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.data.DbContext
import dev.lizainslie.pitohui.core.data.DeveloperOptionsTable
import dev.lizainslie.pitohui.core.data.ModuleSwitchTable
import dev.lizainslie.pitohui.core.fs.BotFS
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter

class Bot(vararg val baseModules: AbstractModule = emptyArray()) {
    init {
        // generate the base folder structure if it doesn't exist
        BotFS.generateBaseStructure()

        // check to ensure that
        Configs.checkConfigs()

        DbContext.connect()
        DbContext.tables += ModuleSwitchTable
        DbContext.tables += DeveloperOptionsTable
        DbContext.migrate()

        baseModules.forEach(::loadModule)
    }

    lateinit var commands: Commands

    val modules = mutableListOf<AbstractModule>()
    val platformAdapters = mutableListOf<PlatformAdapter>()

    fun enablePlatforms(vararg platforms: PlatformAdapter) {
        platforms.forEach { adapter ->
            if (platformAdapters.none { it.key == adapter.key }) platformAdapters.add(adapter)
        }
    }

    suspend fun eachPlatform(block: suspend (platformAdapter: PlatformAdapter) -> Unit) {
        for (platform in platformAdapters) {
            block(platform)
        }
    }

    private fun checkModuleDependencies(module: AbstractModule) {
        // check dependencies
        for (dependency in module.dependencies) {
            if (modules.none { it.name == dependency }) {
                throw IllegalArgumentException("Module ${module.name} depends on module $dependency, which is not loaded")
            }
        }
    }

    fun unloadModule(module: AbstractModule, force: Boolean = false) {
        for (possiblyDependent in modules) {
            if (module.name in possiblyDependent.dependencies) {
                if (!force) // todo branch for warning
                    throw IllegalStateException("Module ${module.name} is required by ${possiblyDependent.name} and cannot be unloaded")
            }
        }
    }

    fun loadModule(module: AbstractModule) {
        if (modules.any { it.name == module.name }) {
            throw IllegalArgumentException("Module with name ${module.name} already loaded")
        }

        DbContext.tables += module.tables
        DbContext.migrate(module.tables)

        checkModuleDependencies(module)

        modules += module
        module.onLoad()
    }

    suspend fun init() {
        commands = Commands(this)

        eachPlatform {
            it.initialize(this)
        }

        for (module in modules) {
            module.onInit(this)

            for (command in module.commands) {
                commands.registerCommand(command, module)
            }
        }
    }

    suspend fun start() {
        eachPlatform {
            it.start(this)
        }
    }
}
