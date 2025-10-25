package io.holonaut.helloserver.todo

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "todos")
class TodoEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var title: String = "",
    var completed: Boolean = false,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
)
{
    @PrePersist
    fun onCreate() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
