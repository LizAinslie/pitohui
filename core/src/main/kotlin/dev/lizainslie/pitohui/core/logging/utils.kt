package dev.lizainslie.pitohui.core.logging

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.AnyPlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import org.slf4j.MDC

suspend fun <T> suspendLogTag(tag: String, block: suspend () -> T): T {
    MDC.put("tag", tag)
    val result = block()
    MDC.remove("tag")
    return result
}

fun <T> logTag(tag: String, block: () -> T): T {
    MDC.put("tag", tag)
    val result = block()
    MDC.remove("tag")
    return result
}

suspend fun <T> suspendLogModule(module: AbstractModule, block: suspend () -> T): T {
    MDC.put("module", module.name)
    val result = block()
    MDC.remove("module")
    return result
}

fun <T> logModule(module: AbstractModule, block: () -> T): T {
    MDC.put("module", module.name)
    val result = block()
    MDC.remove("module")
    return result
}

suspend fun <T> suspendLogPlatform(platform: PlatformKey, block: suspend () -> T): T {
    MDC.put("platform", platform.key)
    val result = block()
    MDC.remove("platform")
    return result
}

fun <T> logPlatform(platform: PlatformKey, block: () -> T): T {
    MDC.put("platform", platform.key)
    val result = block()
    MDC.remove("platform")
    return result
}

suspend fun <T> suspendLogPlatform(platform: AnyPlatformAdapter, block: suspend () -> T) = suspendLogPlatform(platform.key, block)
fun <T> logPlatform(platform: AnyPlatformAdapter, block: () -> T) = logPlatform(platform.key, block)
