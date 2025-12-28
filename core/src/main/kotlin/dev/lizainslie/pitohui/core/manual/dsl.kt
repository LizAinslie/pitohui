@file:OptIn(ExperimentalContracts::class)
package dev.lizainslie.pitohui.core.manual

import dev.lizainslie.pitohui.core.annotations.PitohuiDsl
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PitohuiDsl
class ManualDsl(
    private val identifier: String,
    private val title: String
) {
    private val pages = mutableListOf<ManualPage>()

    fun page(title: String, description: String, pageBuilder: ManualPageDsl.() -> Unit) {
        contract { callsInPlace(pageBuilder, InvocationKind.EXACTLY_ONCE) }
        val page = ManualPageDsl(title, description).apply(pageBuilder).build()
        pages += page
    }

    fun build() =
        Manual(identifier, title, pages)
}

@PitohuiDsl
class ManualPageDsl(var title: String, var description: String) {
    private val sections = mutableListOf<ManualPageSection>()
    private var image: String? = null
    private var summary: String? = null

    fun section(title: String, content: String) {
        sections.add(ManualPageSection(title, content))
    }

    fun section(title: String, createContent: () -> String) {
        contract { callsInPlace(createContent, InvocationKind.EXACTLY_ONCE) }
        sections.add(ManualPageSection(title, createContent()))
    }

    fun build() = ManualPage(title, sections, image, summary)
}


