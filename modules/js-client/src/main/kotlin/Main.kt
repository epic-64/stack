import io.holonaut.shared.Greeting
import kotlinx.browser.document
import kotlin.js.Date

fun main() {
    val greeting = Greeting(
        message = "Hello from Kotlin/JS via shared type!",
        timestampMillis = Date.now().toLong()
    )

    val text = "${'$'}{greeting.message} (ts=${'$'}{greeting.timestampMillis})"

    val root = document.getElementById("app")
    if (root != null) {
        root.textContent = text
    } else {
        // Fallback: append a new node if #app is not present
        val p = document.createElement("p")
        p.textContent = text
        document.body?.appendChild(p)
    }
}
