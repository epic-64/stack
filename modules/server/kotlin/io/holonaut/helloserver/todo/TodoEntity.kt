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
    // optional start time for duration-based todos
    var startAt: Instant? = null,
    // optional duration in milliseconds
    var durationMillis: Long? = null,
    @ManyToMany
    @JoinTable(
        name = "todo_teams",
        joinColumns = [JoinColumn(name = "todo_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    var teams: MutableSet<io.holonaut.helloserver.team.TeamEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    var createdBy: io.holonaut.helloserver.user.UserEntity? = null,
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
