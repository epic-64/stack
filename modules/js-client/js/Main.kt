import io.holonaut.shared.Todo
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import kotlin.js.json

private const val API_BASE = "http://localhost:8080/api/todos"

@Serializable
private data class CreateTodoRequest(val title: String, val completed: Boolean? = null)

@Serializable
private data class UpdateTodoRequest(val title: String? = null, val completed: Boolean? = null)

fun main() {
    val root = (document.getElementById("app") ?: document.body!!) as HTMLElement
    root.innerHTML = "" // clear placeholder
    root.className = Css.appContainer

    val title = document.createElement("h1").apply { textContent = "Stack" }
    val form = buildForm()
    val list = document.createElement("ul") as HTMLUListElement
    list.className = "todoList"

    root.appendChild(title)
    root.appendChild(form.container)
    root.appendChild(list)

    lateinit var refresh: () -> Unit
    refresh = {
        fetchTodos { todos -> renderTodosInto(list, todos, refresh) }
    }

    form.onSubmit { titleText ->
        createTodo(titleText) { refresh() }
    }

    refresh() // initial load
}

private data class FormElements(val container: HTMLElement, val onSubmit: (((String) -> Unit) -> Unit)) {
    fun onSubmit(handler: (String) -> Unit) = onSubmit.invoke(handler)
}

// Build the input form (title box + add button)
private fun buildForm(): FormElements {
    val container = document.createElement("div") as HTMLDivElement
    container.className = "todoForm"
    val input = document.createElement("input") as HTMLInputElement
    input.placeholder = "What needs to be done?"
    input.size = 30
    input.className = "textInput"
    val button = document.createElement("button") as HTMLButtonElement
    button.textContent = "Add"
    button.className = "btn btnPrimary"

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

// Render the list of todos into the provided UL element.
private fun renderTodosInto(list: HTMLUListElement, todos: List<Todo>, refresh: () -> Unit) {
    list.innerHTML = ""
    todos.forEach { todo ->
        list.appendChild(buildTodoListItem(todo, refresh))
    }
}

// Build a single LI representing a todo (summary + actions)
private fun buildTodoListItem(todo: Todo, refresh: () -> Unit): HTMLLIElement {
    val li = document.createElement("li") as HTMLLIElement
    li.className = "todoItem"

    val summary = document.createElement("div") as HTMLDivElement
    summary.className = "todoSummary"
    summary.tabIndex = 0
    summary.setAttribute("role", "button")
    summary.setAttribute("aria-expanded", "false")

    val span = document.createElement("span") as HTMLSpanElement
    span.className = if (todo.completed) "todoTitle completed" else "todoTitle"
    span.textContent = todo.title
    summary.appendChild(span)

    val actions = document.createElement("div") as HTMLDivElement
    actions.className = "todoActions"

    val toggleBtn = document.createElement("button") as HTMLButtonElement
    toggleBtn.className = if (todo.completed) "btn btnSecondary" else "btn btnPrimary"
    toggleBtn.textContent = if (todo.completed) "Mark active" else "Mark done"
    toggleBtn.onclick = { toggleTodo(todo) { refresh() } }

    val del = document.createElement("button") as HTMLButtonElement
    del.className = "btn btnDanger"
    del.textContent = "Delete"
    del.onclick = {
        val id = todo.id
        if (id != null) deleteTodo(id) { refresh() }
    }

    actions.appendChild(toggleBtn)
    actions.appendChild(del)

    fun toggleExpanded() {
        val expanded = li.classList.toggle("expanded")
        summary.setAttribute("aria-expanded", expanded.toString())
    }
    summary.onclick = { toggleExpanded() }
    summary.onkeydown = { e ->
        val ke = e as KeyboardEvent
        if (ke.key == "Enter" || ke.key == " ") {
            ke.preventDefault()
            toggleExpanded()
        }
    }

    li.appendChild(summary)
    li.appendChild(actions)
    return li
}

private fun fetchTodos(done: (List<Todo>) -> Unit) {
    window.fetch(API_BASE)
        .then { resp ->
            if (!resp.ok) throw Throwable("HTTP ${resp.status}")
            resp.text()
        }
        .then { text -> done(Json.decodeFromString(text)) }
}

private fun createTodo(title: String, done: () -> Unit) {
    val body = Json.encodeToString(CreateTodoRequest(title = title))
    val request = RequestInit(
        method = "POST",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    window.fetch(API_BASE, request).then { done() }
}

private fun toggleTodo(todo: Todo, done: () -> Unit) {
    val body = Json.encodeToString(UpdateTodoRequest(completed = !todo.completed))
    val request = RequestInit(
        method = "PATCH",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    window.fetch("${API_BASE}/${todo.id}", request).then { done() }
}

private fun deleteTodo(id: Long, done: () -> Unit) {
    window.fetch("${API_BASE}/$id", RequestInit(method = "DELETE")).then { done() }
}
