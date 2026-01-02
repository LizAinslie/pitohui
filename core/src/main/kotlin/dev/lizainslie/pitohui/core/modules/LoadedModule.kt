package dev.lizainslie.pitohui.core.modules

import java.io.File

data class LoadedModule(
    val name: String,
    val instance: AbstractModule,
    val classLoader: ModuleClassLoader?,
    val source: ModuleSource,
)