package dev.lizainslie.pitohui.core.fs

import dev.lizainslie.pitohui.core.Bot
import java.io.File

object BotFS {
    /**
     * The root bot directory
     */
    val baseDir: File = System.getProperty("pitohui.bot.dir")?.let { File(it) }
        ?: File(Bot::class.java.protectionDomain.codeSource.location.path).parentFile

    /**
     * The directory where hosts are expected to place platform and module
     * configuration files
     */
    val configDir = baseDir.resolve("config")

    val platformConfigDir = configDir.resolve("platforms")
    val moduleConfigDir = configDir.resolve("modules")

    /**
     * The directory where hosts are expected to place modules
     */
    val modulesDir = baseDir.resolve("modules")

    fun generateBaseStructure() {
        // create the config directory & its substructure if it doesn't exist
        if (!configDir.exists()) configDir.mkdirs()
        if (!platformConfigDir.exists()) platformConfigDir.mkdirs()
        if (!moduleConfigDir.exists()) moduleConfigDir.mkdirs()

        // create the modules directory if it doesn't exist
        if (!modulesDir.exists()) modulesDir.mkdirs()
    }

    object Temp {
        /**
         * The directory for temporary files
         */
        val dir: File = File("${System.getProperty("java.io.tmpdir")}/pitohui").absoluteFile
            .apply { if (!exists()) mkdirs() }

        val modules = dir.resolve("modules")
            .apply { if (!exists()) mkdirs() }

        /**
         * Cleans up all files in the temp directory
         */
        fun cleanup() {
            dir.listFiles()?.forEach { it.deleteRecursively() }
        }
    }
}