package dev.lizainslie.pitohui.core.commands.argument

import kotlin.reflect.KProperty

@JvmInline
value class ResolvedArgument<T : Any>(
    val value: T
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}