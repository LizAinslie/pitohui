package dev.lizainslie.pitohui.core.commands.argument.text

import dev.lizainslie.pitohui.core.commands.BaseCommand

class TextArguments(private val command: BaseCommand, raw: String) {
    init {
        // todo: parse arguments into a map
        // todo: validate arguments against command definition
        // todo: store arguments in a way that they can be retrieved by name and type
    }

    companion object {
        fun parse(command: BaseCommand, input: String) =
            TextArguments(command, input)
    }
}