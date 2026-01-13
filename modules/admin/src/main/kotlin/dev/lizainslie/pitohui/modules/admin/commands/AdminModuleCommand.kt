package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.BaseCommandDsl
import dev.lizainslie.pitohui.core.commands.argument.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.fs.BotFS
import dev.lizainslie.pitohui.core.modules.ModuleRegistry
import dev.lizainslie.pitohui.core.modules.ModuleSource
import dev.lizainslie.pitohui.platforms.discord.Discord

internal fun BaseCommandDsl.defineModuleNameArg() = argument("module_name", "The name of the module to reload", ArgumentTypes.STRING) {
    defaultValue = "all"

    complete {
        ModuleRegistry.instance.loadedModules
            .filter { it.source == ModuleSource.EXTERNAL }
            .map { it.name } +
                listOf("all")
    }
}

val AdminModuleCommand = defineCommand(
    name = "admin_module",
    description = "Administrative commands for managing modules",
) {
    platform(Discord)

    subCommand("reload", "Reload a module") {
        val moduleNameArg = defineModuleNameArg()

        handle {
            val moduleName by moduleNameArg.require()

            when (moduleName) {
                "all" -> {
                    val response = respond("Reloading all external modules...")
                    ModuleRegistry.instance.fullReload()
                    response.createFollowup("All external modules reloaded.")
                }

                else -> {
                    if (ModuleRegistry.instance.get(moduleName) == null) {
                        respondError("Module `$moduleName` is not loaded.")
                        return@handle
                    }

                    val response = respond("Reloading module `$moduleName`...")
                    ModuleRegistry.instance.reload(moduleName)
                    response.createFollowup("Module `$moduleName` reloaded.")
                }
            }
        }
    }

    subCommand("unload", "Unload a module") {
        val moduleNameArg = defineModuleNameArg()

        handle {
            val moduleName by moduleNameArg.require()

            when (moduleName) {
                "all" -> {
                    val response = respond("Unloading all external modules...")
                    ModuleRegistry.instance.unloadExternal()
                    response.createFollowup("All external modules unloaded.")
                }

                else -> {
                    val module = ModuleRegistry.instance.get(moduleName)
                    if (module == null) {
                        respondError("Module `$moduleName` is not loaded.")
                        return@handle
                    }

                    if (module.source == ModuleSource.INTERNAL) {
                        respondError("Module `$moduleName` is internal and cannot be unloaded.")
                        return@handle
                    }

                    val response = respond("Unloading module `$moduleName`...")
                    ModuleRegistry.instance.unload(moduleName)
                    response.createFollowup("Module `$moduleName` unloaded.")
                }
            }
        }
    }

    subCommand("list", "List all loaded modules") {
        handle {
            val modules = ModuleRegistry.instance.loadedModules

            if (modules.isEmpty()) {
                respond("No modules are currently loaded.")
                return@handle
            }

            val moduleList = modules.joinToString("\n") { mod ->
                "- ${mod.name} (Source: ${mod.source})"
            }

            respond("Loaded Modules:\n$moduleList")
        }
    }

    subCommand("load_new", "Load a new module from a JAR file") {
        val jarNameArg = argument("jar_name", "The file name of the module JAR", ArgumentTypes.STRING)

        handle {
            val jarName by jarNameArg.require()

            val jarFile = BotFS.modulesDir.resolve(jarName)

            try {
                ModuleRegistry.instance.loadExternalModule(jarFile)
                respond("Module loaded from `${jarFile.absolutePath}`.")
            } catch (e: Exception) {
                respondError("Failed to load module from `${jarFile.absolutePath}`: ${e.message}")
            }
        }
    }
}