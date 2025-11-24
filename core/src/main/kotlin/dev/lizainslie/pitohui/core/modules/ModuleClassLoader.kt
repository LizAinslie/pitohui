package dev.lizainslie.pitohui.core.modules

import java.net.URL
import java.net.URLClassLoader

class ModuleClassLoader(
    jarUrl: URL,
    parent: ClassLoader
) : URLClassLoader(arrayOf(jarUrl), parent) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return try {
            findClass(name)
        } catch (_: ClassNotFoundException) {
            super.loadClass(name, resolve)
        }
    }
}