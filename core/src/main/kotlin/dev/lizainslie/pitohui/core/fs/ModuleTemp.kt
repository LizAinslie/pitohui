package dev.lizainslie.pitohui.core.fs

import java.io.File

class ModuleTemp(val directory: File) {
    val contexts = mutableListOf<ModuleTempContext>()

    init {
        if (directory.exists() && !directory.isDirectory)
            throw IllegalArgumentException("Module temp context path ${directory.path} exists and is not a directory")

        if (!directory.exists())
            directory.mkdirs()
    }

    fun cleanup() {
        contexts.forEach { it.cleanup() }
        contexts.clear()
        directory.deleteRecursively()
    }

    fun createContext(): ModuleTempContext {
        val context = ModuleTempContext(this)
        contexts += context
        return context
    }

    fun removeContext(context: ModuleTempContext) {
        context.cleanup()
        contexts -= context
    }
}