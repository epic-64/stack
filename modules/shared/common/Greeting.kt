package io.holonaut.shared

import kotlinx.serialization.Serializable

/**
 * A tiny data type that can be used from both JVM (Spring) and JS.
 */
@Serializable
data class Greeting(
    val message: String,
    val timestampMillis: Long
)
