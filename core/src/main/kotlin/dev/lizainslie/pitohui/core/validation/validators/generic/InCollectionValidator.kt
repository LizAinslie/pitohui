package dev.lizainslie.pitohui.core.validation.validators.generic

import dev.lizainslie.pitohui.core.validation.ValidationResult
import dev.lizainslie.pitohui.core.validation.Validator

class InCollectionValidator<T : Any>(
    private val collection: Collection<T>,
) : Validator<T> {
    override fun validate(value: T) =
        if (value in collection) ValidationResult.Valid
        else ValidationResult.Invalid(listOf(
            "$value not in ${collection::class.simpleName}[${collection.joinToString(", ")}]",
        ))
}