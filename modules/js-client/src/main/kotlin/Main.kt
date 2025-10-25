import kotlinx.browser.document

fun main() {
    val root = document.getElementById("app")
    if (root != null) {
        root.textContent = "Hello from Kotlin/JS!"
    } else {
        // Fallback: append a new node if #app is not present
        val p = document.createElement("p")
        p.textContent = "Hello from Kotlin/JS! (created element)"
        document.body?.appendChild(p)
    }
}
