import io.holonaut.shared.Greeting
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json

fun main() {
    val root = document.getElementById("app")

    fun render(text: String, isError: Boolean = false) {
        val target = root ?: run {
            val p = document.createElement("p")
            document.body?.appendChild(p)
            p
        }
        target.textContent = text
        // Apply CSS classes for styling
        target.className = "" // reset
        target.classList.add(Css.appContainer)
        if (isError) target.classList.add(Css.error)
    }

    // Fetch Greeting JSON from the backend and deserialize using kotlinx.serialization
    window.fetch("http://localhost:8080/hello")
        .then { response ->
            if (!response.ok) {
                throw Throwable("HTTP ${response.status} ${response.statusText}")
            }
            response.text()
        }
        .then { body ->
            val greeting = Json.decodeFromString<Greeting>(body)
            render("${greeting.message} (ts=${greeting.timestampMillis})")
        }
        .catch { err ->
            render("Failed to load greeting: $err", isError = true)
        }
}
