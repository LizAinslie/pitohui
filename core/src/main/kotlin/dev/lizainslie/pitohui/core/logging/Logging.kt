package dev.lizainslie.pitohui.core.logging

import dev.lizainslie.pitohui.core.config.Configs

object Logging {
    val config by Configs.config<LoggingConfig>()
    fun init() {
        syncLevel(config.level)
    }

    fun syncLevel(level: String) {
        System.setProperty("log.level", level.uppercase())
    }
}