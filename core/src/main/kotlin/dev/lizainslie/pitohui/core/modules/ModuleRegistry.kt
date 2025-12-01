package dev.lizainslie.pitohui.core.modules

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.fs.BotFS
import dev.lizainslie.pitohui.core.logging.logModule
import dev.lizainslie.pitohui.core.logging.suspendLogModule
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

class ModuleRegistry(
    private val bot: Bot
) {
    val loadedModules = mutableListOf<LoadedModule>()
    private val log = LoggerFactory.getLogger(javaClass)

    fun load(module: LoadedModule) {
        logModule(module.instance) {
            if (loadedModules.any { it.name == module.name }) {
                log.warn("Module with name '${module.name}' is already loaded! Skipping.")
                return@logModule
            }

            loadedModules += module
            module.instance.onLoad()

            log.info("Loaded module '${module.name}'.")
        }
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
        log.info("Initializing ${loadedModules.size} modules.")
        loadedModules.sortByDependencies()

        for (module in loadedModules) {
            initialize(module)
        }
    }

    suspend fun initialize(module: LoadedModule) {
        suspendLogModule(module.instance) {
            log.info("Initializing module '${module.name}'.")

            module.instance.onInit(bot)

            bot.commands.registerModuleCommands(module.instance)
        }
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
        suspendLogModule(mod.instance) {
            log.info("Unloading module '${mod.name}'.")

            mod.instance.onUnload()

            bot.commands.unregisterModuleCommands(mod.instance)
            loadedModules.remove(mod)
        }
    }

    suspend fun reload(name: String) {
        val mod = get(name) ?: return
        reload(mod)
    }

    /**
     * Reload `mod` from disk
     */
    suspend fun reload(mod: LoadedModule) {
        suspendLogModule(mod.instance) {
            log.info("Reloading module '${mod.name}'.")
            val name = mod.name
            if (mod.source == ModuleSource.INTERNAL) {
                log.warn("Internal module '$name' cannot be reloaded.")
                return@suspendLogModule
            }

            unload(mod)

            val jar = mod.jarFile ?: run {
                log.error("Module '$name' is missing its JAR file, cannot reload.")
                return@suspendLogModule
            }

            loadExternalModule(jar)

            log.debug("Calling garbage collector to unload old module classes.")
            System.gc() // encourages class unloading

            initialize(name)
            log.info("Reloaded module '${mod.name}'.")
        }
    }

    suspend fun fullReload() {
        log.info("Reloading all modules")
        for (mod in loadedModules.filter { it.source == ModuleSource.EXTERNAL }) {
            reload(mod)
        }
    }

    fun loadJarModules(dir: File = BotFS.modulesDir) {
        log.info("Loading JAR modules from ${dir.absolutePath}")
        dir.listFiles().filter { it.isFile && it.extension == "jar" }.forEach {
            loadExternalModule(it)
        }
    }
}