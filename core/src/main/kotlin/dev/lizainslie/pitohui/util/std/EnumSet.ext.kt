package dev.lizainslie.pitohui.util.std

import java.util.EnumSet

/**
 * Shorthand for [EnumSet].allOf([T]::class.java)
 */
inline fun <reified T : Enum<T>> enumSetAll(): EnumSet<T> {
    return EnumSet.allOf(T::class.java)
}