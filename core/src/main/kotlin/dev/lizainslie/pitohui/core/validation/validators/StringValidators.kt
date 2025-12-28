package dev.lizainslie.pitohui.core.validation.validators

import dev.lizainslie.pitohui.core.validation.Validator

object StringValidators {
    object HexColor : Validator<String> {
        private val hexColorRegex = Regex("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$")

        override fun validate(value: String): Boolean {
            return hexColorRegex.matches(value)
        }

        override fun errorMessage(value: String): String {
            return "'$value' is not a valid hex color. It should be in the format '#RRGGBB' or '#RGB'."
        }
    }
}