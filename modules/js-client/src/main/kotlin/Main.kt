import io.holonaut.shared.Greeting
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

fun main() {
    val root = document.getElementById("app")

    fun render(text: String) {
        if (root != null) {
            root.textContent = text
        } else {
            val p = document.createElement("p")
            p.textContent = text
            document.body?.appendChild(p)
        }
    }

    // Fetch Greeting JSON from the backend and deserialize using kotlinx.serialization
    window.fetch("/hello")
        .then { response ->
            if (!response.ok) {
                throw Throwable("HTTP ${'$'}{response.status}")
            }
            response.text()
        }
        .then { body ->
            val greeting = Json.decodeFromString<Greeting>(body)
            render("${'$'}{greeting.message} (ts=${'$'}{greeting.timestampMillis})")
        }
        .catch { err ->
            render("Failed to load greeting: ${'$'}err")
        }
}
