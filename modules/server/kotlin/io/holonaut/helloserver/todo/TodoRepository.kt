package io.holonaut.helloserver.todo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TodoRepository : JpaRepository<TodoEntity, Long> {
    @Query("select distinct t from TodoEntity t left join t.teams team where t.createdBy.id = :userId or team.id in :teamIds")
    fun findAccessible(@Param("userId") userId: Long, @Param("teamIds") teamIds: Collection<Long>): List<TodoEntity>

    fun findByCreatedBy_Id(userId: Long): List<TodoEntity>
}