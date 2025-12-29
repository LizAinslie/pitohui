@file:OptIn(ExperimentalContracts::class)
package dev.lizainslie.pitohui.core.validation

import dev.lizainslie.pitohui.core.annotations.PitohuiDsl
import dev.lizainslie.pitohui.core.validation.validators.CoercingValidator
import dev.lizainslie.pitohui.core.validation.validators.composite.CompositeValidationMode
import dev.lizainslie.pitohui.core.validation.validators.composite.CompositeValidator
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private interface BaseValidationDsl<T : Any> {
    fun buildValidator(): Validator<T>
}

@PitohuiDsl
class ValidationDsl<T : Any> : BaseValidationDsl<T> {
    private lateinit var _validator: Validator<T>

    val validatorSet get() = ::_validator.isInitialized

    @Throws(IllegalStateException::class)
    fun setValidator(validator: Validator<T>) {
        if (validatorSet)
            throw IllegalStateException("Validator already defined")
        _validator = validator
    }

    @Throws(IllegalStateException::class)
    fun composite(
        mode: CompositeValidationMode = CompositeValidationMode.ALL,
        compositeBuilder: CompositeValidatorDsl<T>.() -> Unit
    ) {
        contract { callsInPlace(compositeBuilder, InvocationKind.EXACTLY_ONCE) }

        val dsl = CompositeValidatorDsl<T>(mode).apply(compositeBuilder)
        setValidator(dsl.buildValidator())
    }

    @Throws(IllegalStateException::class)
    inline fun <reified TNew : Any> isType(
        validationBuilder: ValidationDsl<TNew>.() -> Unit
    ) {
        contract { callsInPlace(validationBuilder, InvocationKind.EXACTLY_ONCE) }

        val dsl = ValidationDsl<TNew>().apply(validationBuilder)
        setValidator(CoercingValidator.create<T, TNew>(dsl.buildValidator()))
    }

    override fun buildValidator(): Validator<T>  {
        if (!validatorSet)
            throw IllegalStateException("You must set a validator!")

        return _validator
    }
}

@PitohuiDsl
class CompositeValidatorDsl<T : Any>(
    private val mode: CompositeValidationMode = CompositeValidationMode.ALL
) : BaseValidationDsl<T> {
    val validators = mutableListOf<Validator<T>>()

    fun addValidator(validator: Validator<T>) {
        validators += validator
    }

    operator fun plus(validator: Validator<T>) {
        addValidator(validator)
    }

    override fun buildValidator() = CompositeValidator(mode, validators)
}

fun <T : Any> buildValidators(
    validatorBuilder: ValidationDsl<T>.() -> Unit
): Validator<T> {
    contract { callsInPlace(validatorBuilder, InvocationKind.EXACTLY_ONCE) }

    val dsl = ValidationDsl<T>().apply(validatorBuilder)
    return dsl.buildValidator()
}