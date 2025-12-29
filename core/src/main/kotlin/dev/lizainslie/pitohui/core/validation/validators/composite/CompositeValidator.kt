package dev.lizainslie.pitohui.core.validation.validators.composite

import dev.lizainslie.pitohui.core.validation.Validator

class CompositeValidator<T : Any>(
    val mode: CompositeValidationMode = CompositeValidationMode.ALL,
    val validators: List<Validator<T>> = emptyList(),
) : Validator<T> {
    constructor(
        mode: CompositeValidationMode = CompositeValidationMode.ALL,
        vararg validators: Validator<T>
    ) : this(mode, validators.toList())

    override fun validate(value: T) =
        mode.collapse(validators.map { it.validate(value) })
}