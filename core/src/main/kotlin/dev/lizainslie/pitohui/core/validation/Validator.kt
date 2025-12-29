package dev.lizainslie.pitohui.core.validation

/**
 * A generic validator interface for validating values of type [T].
 *
 * @param T The type of value to validate.
 */
interface Validator<in T: Any> {
    /**
     * Validate the given [value].
     *
     * @param value The value to validate.
     * @return The [ValidationResult]
     */
    fun validate(value: T): ValidationResult
}