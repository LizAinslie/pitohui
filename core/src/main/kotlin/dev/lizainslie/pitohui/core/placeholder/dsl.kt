@file:OptIn(ExperimentalContracts::class)
package dev.lizainslie.pitohui.core.placeholder

import dev.lizainslie.pitohui.core.annotations.PitohuiDsl
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@PitohuiDsl
class PlaceholderMatrixDsl {
    val matrix = PlaceholderMatrix()

    fun replace(key: String, value: String) {
        matrix.addPlaceholder(key, value)
    }

    fun replace(key: String, getValue: () -> String) {
        matrix.addPlaceholder(key, getValue())
    }
}

fun placeholders(block: PlaceholderMatrixDsl.() -> Unit): PlaceholderMatrix {
    contract { callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE) }
    val dsl = PlaceholderMatrixDsl()
    dsl.block()
    return dsl.matrix
}