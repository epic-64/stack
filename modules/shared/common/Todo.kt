package io.holonaut.shared

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val id: Long? = null,
    val title: String,
    val completed: Boolean = false,
    val createdAtEpochMillis: Long? = null,
    val updatedAtEpochMillis: Long? = null,
)
