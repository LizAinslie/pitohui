package dev.lizainslie.pitohui.core.manual

interface ManualProvider {
    fun registerManPage(man: Manual)

    fun registerManPage(identifier: String, title: String, builder: ManualDsl.() -> Unit) {
        val manual = ManualDsl(identifier, title).apply(builder).build()
        registerManPage(manual)
    }
}