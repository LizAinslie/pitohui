package dev.lizainslie.pitohui.core.validation

fun collapseResultsAll(results: List<ValidationResult>) =
    if (results.all { it is ValidationResult.Valid }) ValidationResult.Valid
    else {
        val collectedErrors = results
            .filterIsInstance<ValidationResult.Invalid>()
            .flatMap {
                it.errors
            }

        ValidationResult.Invalid(collectedErrors)
    }

fun collapseResultsAny(results: List<ValidationResult>) =
    if (results.any { it is ValidationResult.Valid }) ValidationResult.Valid
    else {
        val collectedErrors = results
            .filterIsInstance<ValidationResult.Invalid>()
            .flatMap {
                it.errors
            }

        ValidationResult.Invalid(collectedErrors)
    }

fun collapseResultsOne(results: List<ValidationResult>) =
    results.count { it is ValidationResult.Valid }.let { count ->
        if (count == 1) ValidationResult.Valid
        else {
            val collectedErrors = results
                .filterIsInstance<ValidationResult.Invalid>()
                .flatMap {
                    it.errors
                } + // depending on whether we have more than one, add an extra error
                    if (count > 1) listOf("More than one validator passed")
                    else emptyList()

            ValidationResult.Invalid(collectedErrors)
        }
    }

fun <T : Any> validateValue(
    value: T,
    validator: Validator<T>
) = validator.validate(value)