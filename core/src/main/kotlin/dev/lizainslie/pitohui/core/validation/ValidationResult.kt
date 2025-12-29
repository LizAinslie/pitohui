package dev.lizainslie.pitohui.core.validation

sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(val errors: List<String>) : ValidationResult
}
