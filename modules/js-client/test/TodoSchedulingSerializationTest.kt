import io.holonaut.shared.Todo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class TodoSchedulingSerializationSpec : StringSpec({
    val json = Json

    "serialization handles start only (no duration)" {
        val original = Todo(title = "Start only", startAtEpochMillis = 1_700_000_000_000)
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<Todo>(encoded)
        decoded.startAtEpochMillis shouldBe 1_700_000_000_000
        decoded.durationMillis.shouldBeNull()
        decoded.dueAtEpochMillis.shouldBeNull() // due is computed server side; should stay null in plain DTO
    }

    "serialization handles duration only (no start)" {
        val original = Todo(title = "Duration only", durationMillis = 30 * 60 * 1000L)
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<Todo>(encoded)
        decoded.durationMillis shouldBe 30 * 60 * 1000L
        decoded.startAtEpochMillis.shouldBeNull()
        decoded.dueAtEpochMillis.shouldBeNull()
    }

    "serialization handles both start and duration with manual due provided" {
        val start = 1_700_000_000_000
        val duration = 15 * 60 * 1000L
        val due = start + duration
        val original = Todo(title = "Full schedule", startAtEpochMillis = start, durationMillis = duration, dueAtEpochMillis = due)
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<Todo>(encoded)
        decoded.startAtEpochMillis shouldBe start
        decoded.durationMillis shouldBe duration
        decoded.dueAtEpochMillis shouldBe due
    }

    "defaults remain null when no scheduling provided" {
        val original = Todo(title = "No schedule")
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<Todo>(encoded)
        decoded.startAtEpochMillis.shouldBeNull()
        decoded.durationMillis.shouldBeNull()
        decoded.dueAtEpochMillis.shouldBeNull()
    }
})

