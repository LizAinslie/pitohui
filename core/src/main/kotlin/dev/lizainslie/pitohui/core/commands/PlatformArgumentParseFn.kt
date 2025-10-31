package dev.lizainslie.pitohui.core.commands

fun interface PlatformArgumentParseFn<out T> {
    fun parse(value: Any): T
}