package dev.lizainslie.pitohui.core.manual

import dev.lizainslie.pitohui.core.modules.AbstractModule

class ManualRegistry() {
    val globalManuals: MutableList<Manual> = mutableListOf()
    val moduleManPages: MutableMap<String, MutableList<Manual>> = mutableMapOf()

    fun registerGlobalManPage(man: Manual) {
        globalManuals += man
    }

    fun registerModuleManPage(module: AbstractModule, man: Manual) {
        moduleManPages.getOrPut(module.name) { mutableListOf() } += man
    }

    fun getManPages() =
        globalManuals + moduleManPages.values.flatten()
}