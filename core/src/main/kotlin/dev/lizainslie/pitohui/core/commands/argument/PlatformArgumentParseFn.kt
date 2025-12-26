package dev.lizainslie.pitohui.core.commands.argument

fun interface PlatformArgumentParseFn<out T> {
    fun parse(value: Any): T
}