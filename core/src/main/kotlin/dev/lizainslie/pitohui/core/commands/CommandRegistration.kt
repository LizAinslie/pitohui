package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.modules.AbstractModule

data class CommandRegistration(
    val command: RootCommand,
    val module: AbstractModule,
)
