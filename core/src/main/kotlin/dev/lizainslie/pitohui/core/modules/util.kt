package dev.lizainslie.pitohui.core.modules

// Track visit state for topo sort
internal enum class Mark { TEMP, PERM }

fun MutableList<LoadedModule>.sortByDependencies() {
    // Build a name -> module map for quick lookups.
    val moduleByName = associateBy { it.instance.name }

    val marks = mutableMapOf<String, Mark>()
    val result = ArrayList<LoadedModule>(size)

    fun visit(module: LoadedModule) {
        val name = module.instance.name

        when (marks[name]) {
            Mark.PERM -> return // already sorted
            Mark.TEMP -> error("Circular dependency detected involving module: $name")
            null -> { /* unvisited */ }
        }

        marks[name] = Mark.TEMP

        for (depName in module.instance.dependencies) {
            val dep = moduleByName[depName]
                ?: error("Module '${module.instance.name}' depends on missing module '$depName'.")

            visit(dep)
        }

        marks[name] = Mark.PERM
        result += module
    }

    // Sort all modules
    for (m in this) visit(m)

    // Write sorted order back into this MutableList
    clear()
    addAll(result)
}
