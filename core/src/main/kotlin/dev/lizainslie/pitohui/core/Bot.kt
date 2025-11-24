package dev.lizainslie.pitohui.core

import dev.lizainslie.pitohui.core.commands.Commands
import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.data.DbContext
import dev.lizainslie.pitohui.core.data.DeveloperOptionsTable
import dev.lizainslie.pitohui.core.data.ModuleSwitchTable
import dev.lizainslie.pitohui.core.fs.BotFS
import dev.lizainslie.pitohui.core.logging.Logging
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleRegistry
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter

class Bot(vararg val baseModules: AbstractModule = emptyArray()) {
    val modules = ModuleRegistry(this)
    val commands = Commands(this)

    val platformAdapters = mutableListOf<PlatformAdapter>()

    init {
        // generate the base folder structure if it doesn't exist
        BotFS.generateBaseStructure()

        // check to ensure that configs are valid
        Configs.checkConfigs()

        Logging.init()

        DbContext.connect()
        DbContext.tables += ModuleSwitchTable
        DbContext.tables += DeveloperOptionsTable
        DbContext.migrate()

        baseModules.forEach {
            modules.loadBundledModule(it)
        }

        modules.loadJarModules()
    }

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

    suspend fun init() {
        eachPlatform {
            it.initialize(this)
        }

        modules.initialize()
    }

    suspend fun start() {
        eachPlatform {
            it.start(this)
        }
    }
}
