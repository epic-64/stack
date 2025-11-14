package io.holonaut.shared

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull

class TodoSpec : StringSpec({
    "Todo can be created with only required title field" {
        val todo = Todo(title = "Test task")

        todo.title shouldBe "Test task"
        todo.completed shouldBe false
        todo.id.shouldBeNull()
        todo.createdAtEpochMillis.shouldBeNull()
        todo.updatedAtEpochMillis.shouldBeNull()
        todo.teamIds.shouldBeEmpty()
        todo.startAtEpochMillis.shouldBeNull()
        todo.durationMillis.shouldBeNull()
        todo.dueAtEpochMillis.shouldBeNull()
    }

    "Todo can be created with all fields populated" {
        val todo = Todo(
            id = 42L,
            title = "Complete task",
            completed = true,
            createdAtEpochMillis = 1000000000L,
            updatedAtEpochMillis = 1000001000L,
            teamIds = listOf(1L, 2L, 3L),
            startAtEpochMillis = 1000002000L,
            durationMillis = 3600000L,
            dueAtEpochMillis = 1000005600L
        )

        todo.id shouldBe 42L
        todo.title shouldBe "Complete task"
        todo.completed shouldBe true
        todo.createdAtEpochMillis shouldBe 1000000000L
        todo.updatedAtEpochMillis shouldBe 1000001000L
        todo.teamIds shouldContainExactly listOf(1L, 2L, 3L)
        todo.startAtEpochMillis shouldBe 1000002000L
        todo.durationMillis shouldBe 3600000L
        todo.dueAtEpochMillis shouldBe 1000005600L
    }

    "Todo completed defaults to false when not specified" {
        val todo = Todo(title = "Incomplete task")
        todo.completed shouldBe false
    }

    "Todo teamIds defaults to empty list" {
        val todo = Todo(title = "No teams")
        todo.teamIds.shouldNotBeNull()
        todo.teamIds.shouldBeEmpty()
    }

    "Todo can be associated with multiple teams" {
        val teamIds = listOf(1L, 2L, 3L, 4L, 5L)
        val todo = Todo(title = "Multi-team task", teamIds = teamIds)

        todo.teamIds.apply {
            size shouldBe 5
            shouldContainExactly(teamIds)
        }
    }

    "Todo can have start time without duration" {
        val startTime = 1700000000000L
        val todo = Todo(
            title = "Open-ended task",
            startAtEpochMillis = startTime
        )

        todo.startAtEpochMillis shouldBe startTime
        todo.durationMillis.shouldBeNull()
    }

    "Todo can have duration without start time" {
        val duration = 1800000L // 30 minutes
        val todo = Todo(
            title = "Task with duration",
            durationMillis = duration
        )

        todo.durationMillis shouldBe duration
        todo.startAtEpochMillis.shouldBeNull()
    }

    "Todo timestamps are nullable and not set by default" {
        val todo = Todo(title = "Fresh task")
        todo.createdAtEpochMillis.shouldBeNull()
        todo.updatedAtEpochMillis.shouldBeNull()
    }

    "Todo title can contain special characters" {
        val title = "Fix bug #42: Handle UTF-8 characters like é, ñ, 日本語"
        val todo = Todo(title = title)
        todo.title shouldBe title
    }

    "Todo title can be empty string" {
        val todo = Todo(title = "")
        todo.title shouldBe ""
    }
})
