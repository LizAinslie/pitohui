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
     * @return True if the value is valid, false otherwise.
     */
    fun validate(value: T): Boolean

    /**
     * Get an error message for the given invalid [value].
     *
     * @param value The invalid value.
     * @return An error message describing why the value is invalid.
     */
    fun errorMessage(value: T): String = "Invalid value"
}