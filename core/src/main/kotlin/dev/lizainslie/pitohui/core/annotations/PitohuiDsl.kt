package dev.lizainslie.pitohui.core.annotations

/** [DslMarker] for Pitohui DSLs. */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class PitohuiDsl()
