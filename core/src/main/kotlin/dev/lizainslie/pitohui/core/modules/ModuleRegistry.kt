package dev.lizainslie.pitohui.core.modules

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.fs.BotFS
import kotlinx.serialization.json.Json
import java.io.File

class ModuleRegistry(
    private val bot: Bot
) {
    val loadedModules = mutableListOf<LoadedModule>()

    fun load(module: LoadedModule) {
        loadedModules += module
        module.instance.onLoad()
    }

    fun loadBundledModule(module: AbstractModule) {
        load(
            LoadedModule(
                name = module.name,
                instance = module,
                source = ModuleSource.INTERNAL,
                classLoader = null,
                jarFile = null,
            )
        )
    }

    fun loadExternalModule(file: File) {
        if (!file.exists()) return
        if (!file.isFile) {
            // todo: warn
            return
        }

        if (file.extension != "jar") {
            return
        }

        val url = file.toURI().toURL()
        val cl = ModuleClassLoader(url, Bot::class.java.classLoader)

        val stream = cl.getResourceAsStream("pitohui.module.json")
            ?: error("pitohui.module.json missing in $url")

        val descriptor = Json.decodeFromString<ModuleManifest>(
            stream.reader().readText()
        )

        val cls = cl.loadClass(descriptor.mainClass)
        val instance = cls.getDeclaredField("INSTANCE").get(null) as AbstractModule

        load(
            LoadedModule(
                name = instance.name,
                instance = instance,
                source = ModuleSource.EXTERNAL,
                classLoader = cl,
                jarFile = file,
            )
        )
    }

    fun get(name: String): LoadedModule? = loadedModules.find { it.name == name }

    suspend fun initialize() {
        loadedModules.sortByDependencies()

        for (module in loadedModules) {
            initialize(module)
        }
    }

    suspend fun initialize(module: LoadedModule) {
        module.instance.onInit(bot)
        bot.commands.registerModuleCommands(module.instance)
    }

    suspend fun initialize(name: String) {
        val mod = get(name) ?: return
        initialize(mod)
    }

    suspend fun unload(name: String) {
        val mod = get(name) ?: return
        unload(mod)
    }

    suspend fun unload(mod: LoadedModule) {
        mod.instance.onUnload()
        bot.commands.unregisterModuleCommands(mod.instance)
        loadedModules.remove(mod)
    }

    suspend fun reload(name: String) {
        val mod = get(name) ?: return

        if (mod.source == ModuleSource.INTERNAL) {
            println("Internal module '$name' cannot be reloaded.")
            return
        }

        unload(mod)

        val jar = mod.jarFile ?: error("Missing jar path")

        loadExternalModule(jar)

        System.gc() // encourages class unloading

        initialize(name)
    }

    fun loadJarModules() {
        BotFS.modulesDir.listFiles().filter { it.isFile && it.extension == "jar" }.forEach {
            loadExternalModule(it)
        }
    }
}