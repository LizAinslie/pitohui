package dev.lizainslie.pitohui.core.validation.validators.composite

import dev.lizainslie.pitohui.core.validation.ValidationResult
import dev.lizainslie.pitohui.core.validation.collapseResultsAll
import dev.lizainslie.pitohui.core.validation.collapseResultsAny
import dev.lizainslie.pitohui.core.validation.collapseResultsOne

enum class CompositeValidationMode(val collapse: (List<ValidationResult>) -> ValidationResult) {
    ALL(::collapseResultsAll),
    ANY(::collapseResultsAny),
    ONE(::collapseResultsOne),
    ;

}