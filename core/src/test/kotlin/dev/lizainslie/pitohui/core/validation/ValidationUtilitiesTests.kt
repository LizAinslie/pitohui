package dev.lizainslie.pitohui.core.validation

import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationUtilitiesTests {
    @Test
    fun `collapseResultsAll properly flatMaps all errors into new result`() {
        val before = listOf(
            ValidationResult.Valid,
            ValidationResult.Invalid(listOf(
                "fuck",
                "uhh something broke"
            )),
            ValidationResult.Invalid(listOf(
                "ow"
            )),
        )

        val after = ValidationResult.Invalid(listOf(
            "fuck",
            "uhh something broke",
            "ow"
        ))

        val result = collapseResultsAll(before)

        assertEquals(after, result)
    }

    @Test
    fun `collapseResultsOne only allows one result to be valid`() {
        val before = listOf(
            ValidationResult.Valid,
            ValidationResult.Invalid(listOf(
                "fuck",
                "uhh something broke"
            )),
            ValidationResult.Invalid(listOf(
                "ow"
            )),
        )

        val result = collapseResultsOne(before)

        assertEquals(ValidationResult.Valid, result)

        val beforeFailing = listOf(
            ValidationResult.Valid,
            ValidationResult.Invalid(listOf(
                "fuck",
                "uhh something broke"
            )),
            ValidationResult.Valid,
        )

        val afterFailing = ValidationResult.Invalid(listOf(
            "fuck",
            "uhh something broke",
            "More than one validator passed"
        ))

        val resultFailing = collapseResultsOne(beforeFailing)

        assertEquals(afterFailing, resultFailing)
    }

    @Test
    fun `collapseResultsAny validates when not all incoming results are valid`() {
        val before = listOf(
            ValidationResult.Valid,
            ValidationResult.Invalid(listOf(
                "fuck",
                "uhh something broke"
            )),
            ValidationResult.Invalid(listOf(
                "ow"
            )),
        )

        val result = collapseResultsAny(before)

        assertEquals(ValidationResult.Valid, result)
    }
}