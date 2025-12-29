package dev.lizainslie.pitohui.core.validation.validators

import dev.lizainslie.pitohui.core.validation.ValidationResult
import dev.lizainslie.pitohui.core.validation.Validator
import kotlin.reflect.KClass

/**
 * A validator that ensures a value of known type [TOld] is of actual type
 * [TNew].
 *
 * @param TOld The type of the value validated
 * @param TNew The type we're validating against
 */
class CoercingValidator<TOld : Any, TNew : Any>(
    val innerValidator: Validator<TNew>,
    private val newClass: KClass<TNew>,
) : Validator<TOld> {
    @Suppress("UNCHECKED_CAST")
    override fun validate(value: TOld) = // this is fucking horrible I am so sorry
        if (value::class == newClass) {
            val newValue = value as? TNew ?:
                return ValidationResult.Invalid(
                    listOf(
                        "Cast from ${value::class.simpleName} to ${newClass.simpleName}"
                    )
                )
            innerValidator.validate(newValue)
        } else ValidationResult.Invalid(
            listOf(
                "Class of value $value is not of type ${newClass.simpleName}. Actual: ${value::class.simpleName}"
            )
        )

    companion object {
        inline fun <
                TOld : Any,
                reified TNew : Any
        > create(validator: Validator<TNew>) =
            CoercingValidator<TOld, TNew>(
                validator,
                TNew::class
            )
    }
}