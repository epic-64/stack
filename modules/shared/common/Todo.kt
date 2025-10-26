package io.holonaut.shared

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val id: Long? = null,
    val title: String,
    val completed: Boolean = false,
    val createdAtEpochMillis: Long? = null,
    val updatedAtEpochMillis: Long? = null,
    val teamIds: List<Long> = emptyList(),
    // start instant in epoch millis (optional)
    val startAtEpochMillis: Long? = null,
    // duration in milliseconds (optional)
    val durationMillis: Long? = null,
    // computed due time (not stored in DB) – server fills when start+duration present
    val dueAtEpochMillis: Long? = null,
)
