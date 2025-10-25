package io.holonaut.shared

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val username: String,
    val teamIds: List<Long> = emptyList(),
)

