package io.holonaut.helloserver.todo

import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<TodoEntity, Long>
