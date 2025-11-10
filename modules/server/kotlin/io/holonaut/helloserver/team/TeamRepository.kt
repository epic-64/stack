package io.holonaut.helloserver.team

import org.springframework.data.jpa.repository.JpaRepository

interface TeamRepository : JpaRepository<TeamEntity, Long> {
    fun existsByName(name: String): Boolean
}
