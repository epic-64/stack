package io.holonaut.shared

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TodoSpec : StringSpec({
    "Todo can be created with required fields" {
        val todo = Todo(
            title = "Test task",
            completed = false
        )
        todo.title shouldBe "Test task"
        todo.completed shouldBe false
        todo.id shouldBe null
    }

    "Todo with start time and duration computes correctly" {
        val startTime = 1000000000L
        val duration = 3600000L // 1 hour in millis
        val todo = Todo(
            title = "Scheduled task",
            startAtEpochMillis = startTime,
            durationMillis = duration
        )
        todo.startAtEpochMillis shouldBe startTime
        todo.durationMillis shouldBe duration
    }

    "Todo teamIds defaults to empty list" {
        val todo = Todo(title = "No teams")
        todo.teamIds shouldBe emptyList()
    }

    "Todo can be created with team associations" {
        val teamIds = listOf(1L, 2L, 3L)
        val todo = Todo(
            title = "Team task",
            teamIds = teamIds
        )
        todo.teamIds shouldBe teamIds
        todo.teamIds.size shouldBe 3
    }
})
