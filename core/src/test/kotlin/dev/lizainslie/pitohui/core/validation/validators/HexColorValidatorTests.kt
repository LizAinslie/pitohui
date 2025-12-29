package dev.lizainslie.pitohui.core.validation.validators

import dev.lizainslie.pitohui.core.validation.ValidationResult
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HexColorValidatorTests {
    val alphaHexColorValidator = StringValidators.HexColor(true)
    val hexColorValidator = StringValidators.HexColor(false)

    @Test
    fun `HexColor properly validates shorthand with hash`() {
        val validColor = "#Fff"

        val validResult = hexColorValidator.validate(validColor)

        assertEquals(ValidationResult.Valid, validResult)

        val invalidColor = "#Fffa"

        val invalidResult = hexColorValidator.validate(invalidColor)
        val invalidExpected = ValidationResult.Invalid(listOf(
            "'$invalidColor' is not a valid hex color. It should be in the format '#RRGGBB' or '#RGB'."
        ))

        assertEquals(invalidExpected, invalidResult)
    }

    @Test
    fun `HexColor properly validates full color with hash`() {
        val validColor = "#Dc3545"

        val validResult = hexColorValidator.validate(validColor)

        assertEquals(ValidationResult.Valid, validResult)

        val invalidColor = "#Dc3545aa"

        val invalidResult = hexColorValidator.validate(invalidColor)
        val invalidExpected = ValidationResult.Invalid(listOf(
            "'$invalidColor' is not a valid hex color. It should be in the format '#RRGGBB' or '#RGB'."
        ))

        assertEquals(invalidExpected, invalidResult)
    }
}