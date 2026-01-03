package dev.lizainslie.pitohui.core.modules
// last incident: 2026/01/02

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.data.DbContext
import dev.lizainslie.pitohui.core.fs.BotFS
import dev.lizainslie.pitohui.core.logging.logModule
import dev.lizainslie.pitohui.core.logging.suspendLogModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

class ModuleRegistry(
    private val bot: Bot
) {
    val loadedModules = mutableListOf<LoadedModule>()
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        instance = this
    }

    fun load(module: LoadedModule) {
        logModule(module.instance) {
            if (loadedModules.any { it.name == module.name }) {
                log.warn("Module with name '${module.name}' is already loaded! Skipping.")
                return@logModule
            }

            log.info("Running migrations for module '${module.name}'...")
            DbContext.migrate(module.instance.tables)

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
            )
        )
    }

    suspend fun loadExternalModule(file: File) {
        withContext(Dispatchers.IO) {
            log.info("Loading external module from ${file.absolutePath}...")

            if (!file.exists()) return@withContext // this should never hit, just like me with pretty women

            if (!file.isFile) {
                log.warn("Module path ${file.absolutePath} is not a file, skipping.")
                return@withContext
            }

            if (file.extension != "jar") {
                log.warn("Module file ${file.absolutePath} is not a JAR file, skipping.")
                return@withContext
            }

            val url = file.toURI().toURL()
            val cl = ModuleClassLoader(url, Bot::class.java.classLoader)

            val stream = cl.getResourceAsStream("pitohui.module.json")
                ?: run {
                    log.warn("Module JAR ${file.absolutePath} is missing manifest file 'pitohui.module.json', skipping.")
                    return@withContext
                }

            val descriptor = Json.decodeFromString<ModuleManifest>(
                stream.reader().readText()
            )

            log.debug("Successfully read manifest. Module main class: ${descriptor.mainClass}")

            val cls = cl.loadClass(descriptor.mainClass)
            val instance = cls.getDeclaredField("INSTANCE").get(null) as AbstractModule

            log.debug("Module class loaded, name: '${instance.name}'")

            load(
                LoadedModule(
                    name = instance.name,
                    instance = instance,
                    source = ModuleSource.EXTERNAL,
                    classLoader = cl,
                )
            )
        }
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

    suspend fun unloadAll() {
        log.info("Unloading all modules.")
        for (mod in loadedModules) {
            unload(mod)
        }
    }

    suspend fun unloadExternal() {
        log.info("Unloading all external modules.")
        for (mod in loadedModules.filter { it.source == ModuleSource.EXTERNAL }) {
            unload(mod)
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

            val jar = BotFS.modulesDir.resolve("${mod.name}.jar").also {
                if (!it.exists()) run {
                    log.error("Module '$name' is missing its JAR file, cannot reload.")
                    return@suspendLogModule
                }
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

    suspend fun loadJarModules(dir: File = BotFS.modulesDir) {
        log.info("Loading JAR modules from ${dir.absolutePath}")
        withContext(Dispatchers.IO) {
            dir.listFiles().filter { it.isFile && it.extension == "jar" }.forEach {
                loadExternalModule(it)
            }
        }
    }

    companion object {
        lateinit var instance: ModuleRegistry
    }
}