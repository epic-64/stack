package io.holonaut.helloserver.team

import io.holonaut.helloserver.todo.TodoEntity
import io.holonaut.helloserver.user.UserEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "teams")
class TeamEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true, nullable = false)
    var name: String = "",
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    @ManyToMany(mappedBy = "teams", fetch = FetchType.EAGER)
    var members: MutableSet<UserEntity> = mutableSetOf(),
    @ManyToMany(mappedBy = "teams")
    var todos: MutableSet<TodoEntity> = mutableSetOf(),
) {
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
