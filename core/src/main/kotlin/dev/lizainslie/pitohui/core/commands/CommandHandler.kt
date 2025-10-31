package dev.lizainslie.pitohui.core.commands

typealias CommandHandler = suspend CommandContext.() -> Unit
