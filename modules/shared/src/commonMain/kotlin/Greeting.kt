package io.holonaut.shared

/**
 * A tiny data type that can be used from both JVM (Spring) and JS.
 */
data class Greeting(
    val message: String,
    val timestampMillis: Long
)
