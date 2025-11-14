import kotlinx.browser.document
import org.w3c.dom.*

inline fun <reified T : HTMLElement> el(tag: String, classes: String? = null, block: T.() -> Unit = {}): T {
    val node = document.createElement(tag) as T
    if (!classes.isNullOrBlank()) node.className = classes
    node.block()
    return node
}

data class FormElements(
    val container: HTMLElement,
    val onSubmit: (((String) -> Unit) -> Unit)
) {
    fun onSubmit(handler: (String) -> Unit) = onSubmit.invoke(handler)
}

fun buildForm(): FormElements {
    val container = el<HTMLDivElement>("div", "todoForm")
    val input = el<HTMLInputElement>("input", "textInput") {
        placeholder = "What needs to be done?"
        size = 30
    }
    val button = el<HTMLButtonElement>("button", "btn btnPrimary") { textContent = "Add" }
    container.appendChild(input)
    container.appendChild(button)

    var submitHandler: (String) -> Unit = {}
    fun submit() {
        val text = input.value.trim()
        if (text.isNotEmpty()) {
            submitHandler(text)
            input.value = ""
            input.focus()
        }
    }
    button.onclick = { submit() }
    input.onkeypress = { e -> if (e.key == "Enter") submit() }

    return FormElements(container) { handler -> submitHandler = handler }
}

