import io.holonaut.shared.Todo
import io.holonaut.shared.User
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import kotlin.js.Date
import kotlin.js.json
import kotlin.math.roundToInt

private const val API_BASE = "http://localhost:8080/api/todos"

@Serializable
private data class CreateTodoRequest(val title: String, val completed: Boolean? = null)

@Serializable
private data class UpdateTodoRequest(val title: String? = null, val completed: Boolean? = null)

private var progressIntervals: MutableList<Int> = mutableListOf()

fun main() {
    val root = (document.getElementById("app") ?: document.body!!) as HTMLElement
    root.innerHTML = ""
    root.className = Css.appContainer
    initAuth(root) { user ->
        renderApp(root, user)
    }
}

// -------------------------------------------------------------
// Main App (Todos) once authenticated
// -------------------------------------------------------------
private fun renderApp(root: HTMLElement, user: User) {
    root.innerHTML = ""

    val headerRow = document.createElement("div") as HTMLDivElement
    headerRow.style.display = "flex"
    headerRow.style.alignItems = "center"
    headerRow.style.justifyContent = "space-between"
    headerRow.style.marginBottom = "14px"

    val title = document.createElement("h1").apply { textContent = "Stack – ${user.username}" }

    val logoutBtn = document.createElement("button") as HTMLButtonElement
    logoutBtn.className = "btn btnSecondary"
    logoutBtn.textContent = "Logout"
    logoutBtn.onclick = {
        logout(root)
    }

    headerRow.appendChild(title)
    headerRow.appendChild(logoutBtn)

    val form = buildForm()
    val list = document.createElement("ul") as HTMLUListElement
    list.className = "todoList"

    root.appendChild(headerRow)
    root.appendChild(form.container)
    root.appendChild(list)

    lateinit var refresh: () -> Unit
    refresh = {
        fetchTodos { todos -> renderTodosInto(list, todos, refresh) }
    }

    form.onSubmit { titleText ->
        createTodo(titleText) { refresh() }
    }

    refresh()
}

// -------------------------------------------------------------
// Existing todo UI helpers (mostly unchanged except auth wrapper on network)
// -------------------------------------------------------------
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
    clearProgressIntervals()
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

    // progress bar (only if start + duration present)
    if (todo.startAtEpochMillis != null && todo.durationMillis != null) {
        val start: Long = todo.startAtEpochMillis!!
        val duration: Long = todo.durationMillis!!
        val end = start + duration
        val progressContainer = document.createElement("div") as HTMLDivElement
        progressContainer.className = "progressContainer"
        val progressFill = document.createElement("div") as HTMLDivElement
        progressFill.className = "progressFill"
        var intervalHandle: Int? = null
        fun updateBar() {
            val now = Date.now().toLong()
            val remainingPercent = when {
                now <= start -> 100
                now >= end -> 0
                else -> (((end - now).toDouble() / (end - start).toDouble()) * 100).roundToInt()
            }
            progressFill.style.width = remainingPercent.coerceIn(0, 100).toString() + "%"
            if (remainingPercent <= 0) {
                intervalHandle?.let { window.clearInterval(it) }
                intervalHandle = null
            }
        }
        updateBar()
        intervalHandle = window.setInterval({ updateBar() }, 1000)
        intervalHandle?.let { progressIntervals.add(it) }
        progressContainer.appendChild(progressFill)
        summary.appendChild(progressContainer)
    }

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
    authedFetch(API_BASE)
        .then { resp ->
            if (resp.status == 401.toShort()) {
                // Re-authenticate
                val root = (document.getElementById("app") ?: document.body!!) as HTMLElement
                logout(root)
                null
            } else if (!resp.ok) throw Throwable("HTTP ${resp.status}") else resp.text()
        }
        .then { anyText ->
            if (anyText != null) {
                val text = anyText as String
                done(Json.decodeFromString(text))
            }
        }
}

private fun createTodo(title: String, done: () -> Unit) {
    val body = Json.encodeToString(CreateTodoRequest(title = title))
    val request = RequestInit(
        method = "POST",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    authedFetch(API_BASE, request).then { done() }
}

private fun toggleTodo(todo: Todo, done: () -> Unit) {
    val body = Json.encodeToString(UpdateTodoRequest(completed = !todo.completed))
    val request = RequestInit(
        method = "PATCH",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    authedFetch("${API_BASE}/${todo.id}", request).then { done() }
}

private fun deleteTodo(id: Long, done: () -> Unit) {
    authedFetch("${API_BASE}/$id", RequestInit(method = "DELETE")).then { done() }
}

private fun clearProgressIntervals() {
    progressIntervals.forEach { window.clearInterval(it) }
    progressIntervals.clear()
}
