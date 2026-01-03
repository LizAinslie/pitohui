package dev.lizainslie.pitohui.core.commands.argument

import dev.lizainslie.pitohui.core.platforms.AnyPlatformAdapter
import dev.lizainslie.pitohui.core.validation.ValidationResult
import kotlin.reflect.KProperty

class ResolvedArgument<T : Any>(
    val descriptor: ArgumentDescriptor<T>,
    val value: T?
) {
    fun validate() = value?.let { descriptor.validator?.validate(it) } ?: ValidationResult.Valid

    companion object {
        fun <T : Any> resolve(argument: ArgumentDescriptor<T>, value: Any?, platform: AnyPlatformAdapter) =
            ResolvedArgument(
                descriptor = argument,
                value = value?.let {
                    argument.argumentType.tryParse(it, platform)
                        .getOrDefault(argument.defaultValue)
                } ?: argument.defaultValue,
            )
    }
}