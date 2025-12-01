package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.fs.BotFS
import dev.lizainslie.pitohui.core.modules.ModuleRegistry
import dev.lizainslie.pitohui.core.modules.ModuleSource

val AdminModuleCommand = defineCommand(
    name = "admin_module",
    description = "Administrative commands for managing modules",
) {
    subCommand("reload", "Reload a module") {
        val moduleNameArg = argument("module_name", "The name of the module to reload", ArgumentTypes.STRING, "all") {
            ModuleRegistry.instance.loadedModules
                .filter { it.source == ModuleSource.EXTERNAL }
                .map { it.name } +
                    listOf("all")
        }

        handle {
            var moduleName = args[moduleNameArg]

            when (moduleName) {
                null,
                "all" -> {
                    ModuleRegistry.instance.fullReload()
                    respond("All external modules reloaded.")
                }

                else -> {
                    if (ModuleRegistry.instance.get(moduleName) == null) {
                        respondError("Module `$moduleName` is not loaded.")
                        return@handle
                    }

                    ModuleRegistry.instance.reload(moduleName)

                    respond("Module `$moduleName` reloaded.")
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
        val jarName = argument("jar_name", "The file name of the module JAR", ArgumentTypes.STRING)

        handle {
            val jarName = args[jarName] ?: run {
                respondError("JAR name is required.")
                return@handle
            }

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