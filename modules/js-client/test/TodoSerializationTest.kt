import io.holonaut.shared.Todo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TodoSerializationTest {
    @Test
    fun roundTrip_preservesAllFields() {
        val original = Todo(
            id = 42L,
            title = "Write JS client test",
            completed = true,
            createdAtEpochMillis = 111L,
            updatedAtEpochMillis = 222L,
        )
        val json = Json.encodeToString(original)
        val decoded = Json.decodeFromString<Todo>(json)
        assertEquals(original, decoded, "Serialized then deserialized Todo should match original")
        assertTrue(json.contains("Write JS client test"))
    }

    @Test
    fun defaults_areAppliedWhenMissing() {
        // Only required field title
        val json = """{"title":"Only title"}"""
        val decoded = Json.decodeFromString<Todo>(json)
        assertEquals("Only title", decoded.title)
        assertEquals(false, decoded.completed, "Default for completed should be false")
        assertEquals(null, decoded.id)
    }
}

