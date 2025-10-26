import io.holonaut.shared.Todo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.string.shouldContain

class TodoSerializationSpec : StringSpec({
    val json = Json // default configuration

    "round trip serialization preserves all fields including scheduling" {
        val original = Todo(
            id = 42L,
            title = "Write JS client test",
            completed = true,
            createdAtEpochMillis = 111L,
            updatedAtEpochMillis = 222L,
            startAtEpochMillis = 1000L,
            durationMillis = 5000L,
            dueAtEpochMillis = 6000L,
        )
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<Todo>(encoded)

        decoded shouldBe original
        encoded shouldContain "Write JS client test"
        // spot check a couple of fields explicitly for clarity
        decoded.id shouldBe 42L
        decoded.completed shouldBe true
        decoded.dueAtEpochMillis shouldBe 6000L
    }

    "missing optional fields apply defaults" {
        val encoded = """{"title":"Only title"}"""
        val decoded = json.decodeFromString<Todo>(encoded)
        decoded.title shouldBe "Only title"
        decoded.completed shouldBe false
        decoded.id.shouldBeNull()
        decoded.createdAtEpochMillis.shouldBeNull()
        decoded.updatedAtEpochMillis.shouldBeNull()
        decoded.startAtEpochMillis.shouldBeNull()
        decoded.durationMillis.shouldBeNull()
        decoded.dueAtEpochMillis.shouldBeNull()
    }

    "explicit defaults serialize and deserialize identically" {
        val withDefaults = Todo(title = "Just a title")
        val encoded = json.encodeToString(withDefaults)
        val decoded = json.decodeFromString<Todo>(encoded)
        decoded shouldBe withDefaults
        // completed defaults to false
        decoded.completed shouldBe false
    }
})
