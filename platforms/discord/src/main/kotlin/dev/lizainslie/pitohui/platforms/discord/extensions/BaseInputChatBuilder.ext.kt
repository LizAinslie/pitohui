package dev.lizainslie.pitohui.platforms.discord.extensions

import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.RootInputChatBuilder
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.channel
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.role
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import dev.lizainslie.pitohui.core.commands.ArgumentDescriptor
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.SubCommand
import kotlin.collections.forEach

fun BaseInputChatBuilder.arguments(arguments: List<ArgumentDescriptor<*>>) {
    arguments.forEach { argument ->
        when (argument.argumentType) {
            is ArgumentTypes.STRING -> {
                string(argument.name, argument.description) {
                    required = argument.required
                }
            }

            is ArgumentTypes.INT -> {
                integer(argument.name, argument.description) {
                    required = argument.required
                }
            }

            is ArgumentTypes.BOOLEAN -> {
                boolean(argument.name, argument.description) {
                    required = argument.required
                }
            }

            is ArgumentTypes.CHANNEL -> {
                channel(argument.name, argument.description) {
                    required = argument.required
                }
            }

            is ArgumentTypes.ROLE -> {
                role(argument.name, argument.description) {
                    required = argument.required
                }
            }
        }
    }
}

fun RootInputChatBuilder.subCommands(subCommands: List<SubCommand>) {
    subCommands.forEach { subCommand ->
        subCommand(subCommand.name, subCommand.description) {
            arguments(subCommand.arguments)
        }
    }
}
