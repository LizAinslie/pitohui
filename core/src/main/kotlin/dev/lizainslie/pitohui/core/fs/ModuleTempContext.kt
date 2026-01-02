package dev.lizainslie.pitohui.core.fs

import java.io.File
import java.util.UUID

class ModuleTempContext(parent: ModuleTemp) {
    val uuid: UUID = UUID.randomUUID()
    val directory: File = File(parent.directory, uuid.toString())

    init {
        directory.mkdirs()
    }

    fun cleanup() {
        directory.deleteRecursively()
    }

    fun file(relativePath: String) =
        File(directory, relativePath)
}