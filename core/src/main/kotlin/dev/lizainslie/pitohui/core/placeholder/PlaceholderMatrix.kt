package dev.lizainslie.pitohui.core.placeholder

class PlaceholderMatrix {
    val placeholders: MutableMap<String, String> = mutableMapOf()

    fun addPlaceholder(key: String, value: String) {
        placeholders[key] = value
    }

    fun replace(input: String): String {
        var result = input

        for ((key, value) in placeholders)
            result = result.replace("{$key}", value)

        return result
    }
}