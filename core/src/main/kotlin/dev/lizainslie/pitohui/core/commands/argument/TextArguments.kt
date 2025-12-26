package dev.lizainslie.pitohui.core.commands.argument

import dev.lizainslie.pitohui.core.commands.BaseCommand

class TextArguments(private val command: BaseCommand, raw: String) {
    init {

    }

    companion object {
        fun parse(command: BaseCommand, input: String) =
            TextArguments(command, input)
    }
}