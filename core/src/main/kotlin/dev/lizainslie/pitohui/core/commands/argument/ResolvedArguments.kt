package dev.lizainslie.pitohui.core.commands.argument

class ResolvedArguments(val values: Set<ResolvedArgument<*>>) {
    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified T : Any> get(descriptor: ArgumentDescriptor<T>): ResolvedArgument<T> {
        val argument = values.find {
            it.descriptor.name == descriptor.name && it.descriptor.tClass == descriptor.tClass
        }

        if (argument == null) {
            throw Exception("Could not find argument for descriptor ${descriptor.name}")
        }

        return argument as ResolvedArgument<T>
    }
}