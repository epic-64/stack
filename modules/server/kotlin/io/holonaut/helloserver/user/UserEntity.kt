package io.holonaut.helloserver.user

import io.holonaut.helloserver.team.TeamEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true, nullable = false)
    var username: String = "",
    @Column(nullable = false)
    var passwordHash: String = "",
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_teams",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    var teams: MutableSet<TeamEntity> = mutableSetOf(),
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
