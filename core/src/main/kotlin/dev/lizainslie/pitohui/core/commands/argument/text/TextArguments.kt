package dev.lizainslie.pitohui.core.commands.argument.text

import dev.lizainslie.pitohui.core.commands.BaseCommand

class TextArguments(private val command: BaseCommand, raw: String) {
    private val cursor = Cursor(raw)

    init {
        while (cursor.hasNext) {
            val char = cursor.next()


        }
    }

    companion object {
        fun parse(command: BaseCommand, input: String) =
            TextArguments(command, input)
    }
}