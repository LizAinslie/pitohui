package dev.lizainslie.pitohui.platforms.discord.extensions

import dev.kord.core.entity.ReactionEmoji

fun ReactionEmoji.getIdentifier(): String {
    return when (this) {
        is ReactionEmoji.Unicode -> name
        is ReactionEmoji.Custom -> id.value.toString()
    }
}