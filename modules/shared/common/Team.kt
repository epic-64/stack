package io.holonaut.shared

import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: Long? = null,
    val name: String,
    val createdAtEpochMillis: Long? = null,
    val updatedAtEpochMillis: Long? = null,
)
