package dev.lizainslie.pitohui.core.validation.validators

import dev.lizainslie.pitohui.core.validation.ValidationResult
import dev.lizainslie.pitohui.core.validation.Validator

object StringValidators {
    class HexColor(private val allowAlpha: Boolean = false) : Validator<String> {
        override fun validate(value: String) =
            if (getRegex(allowAlpha).matches(value)) ValidationResult.Valid
            else ValidationResult.Invalid(
                listOf(
                    "'$value' is not a valid hex color. It should be in the format '#RRGGBB${if (allowAlpha) "(AA)" else ""}' or '#RGB${if (allowAlpha) "(A)" else ""}'."
                )
            )

        companion object {
            private fun getRegex(allowAlpha: Boolean) = if (allowAlpha) hexColorWithAlphaRegex else hexColorRegex
            private val hexColorRegex = Regex("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$")
            private val hexColorWithAlphaRegex = Regex("^#([a-fA-F0-9]{8}|[a-fA-F0-9]{4})$")
        }
    }
}