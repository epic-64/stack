import io.holonaut.shared.Todo
import io.holonaut.shared.User
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import org.w3c.fetch.RequestInit
import kotlin.js.Date
import kotlin.js.json
import kotlin.math.roundToInt

private const val API_BASE = "http://localhost:8080/api/todos"

@Serializable
private data class CreateTodoRequest(
    val title: String,
    val completed: Boolean? = null
)

@Serializable
private data class UpdateTodoRequest(
    val title: String? = null,
    val completed: Boolean? = null,
    val startAtEpochMillis: Long? = null,
    val durationMillis: Long? = null,
)

private var progressIntervals: MutableList<Int> = mutableListOf()

private fun Int.pad2() = if (this < 10) "0$this" else toString()

private data class ParsedDateTime(val date: String, val hour: String, val minute: String)

private fun parseMillisToDateTime(ms: Long): ParsedDateTime {
    val d = Date(ms.toDouble())
    val year = d.getFullYear()
    val month = (d.getMonth() + 1).pad2()
    val day = d.getDate().pad2()
    val hour = d.getHours().pad2()
    val minute = d.getMinutes().pad2()
    return ParsedDateTime("$year-$month-$day", hour, minute)
}

private inline fun <reified T : HTMLElement> el(tag: String, classes: String? = null, block: T.() -> Unit = {}): T {
    val node = document.createElement(tag) as T
    if (!classes.isNullOrBlank()) node.className = classes
    node.block()
    return node
}

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

    val headerRow = el<HTMLDivElement>("div", "todoHeaderRow")
    val title = el<HTMLHeadingElement>("h1") { textContent = "Stack – ${user.username}" }
    val logoutBtn = el<HTMLButtonElement>("button", "btn btnSecondary") {
        textContent = "Logout"
        onclick = { logout(root) }
    }
    headerRow.appendChild(title)
    headerRow.appendChild(logoutBtn)

    val form = buildForm()
    val list = el<HTMLUListElement>("ul", "todoList")
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
private data class FormElements(
    val container: HTMLElement,
    val onSubmit: (((String) -> Unit) -> Unit)
) { // simplified signature
    fun onSubmit(handler: (String) -> Unit) = onSubmit.invoke(handler)
}

// Build the input form (title box + add button)
private fun buildForm(): FormElements {
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
    val li = el<HTMLLIElement>("li", "todoItem")
    val summary = el<HTMLDivElement>("div", "todoSummary") {
        tabIndex = 0
        setAttribute("role", "button")
        setAttribute("aria-expanded", "false")
    }
    val span = el<HTMLSpanElement>("span", if (todo.completed) "todoTitle completed" else "todoTitle") {
        textContent = todo.title
    }
    summary.appendChild(span)

    // progress bar (only if start + duration present)
    val progressContainer = todo.startAtEpochMillis?.let { start ->
        todo.durationMillis?.let { duration ->
            val end = start + duration
            el<HTMLDivElement>("div", "progressContainer").apply {
                val progressFill = el<HTMLDivElement>("div", "progressFill")
                var intervalHandle: Int? = null
                fun updateBar() {
                    val now = Date.now().toLong()
                    val remainingPercent = when {
                        now <= start -> 100
                        now >= end -> 0
                        else -> (((end - now).toDouble() / (end - start).toDouble()) * 100).roundToInt()
                    }
                    progressFill.style.width = "${remainingPercent.coerceIn(0, 100)}%"
                    if (remainingPercent <= 0) {
                        intervalHandle?.let { window.clearInterval(it) }
                        intervalHandle = null
                    }
                }
                updateBar()
                intervalHandle = window.setInterval({ updateBar() }, 1000)
                intervalHandle?.let { progressIntervals.add(it) }
                appendChild(progressFill)
            }
        }
    }

    val actions = el<HTMLDivElement>("div", "todoActions")
    val toggleBtn = el<HTMLButtonElement>("button", if (todo.completed) "btn btnSecondary" else "btn btnPrimary") {
        textContent = if (todo.completed) "Mark active" else "Mark done"
        onclick = { toggleTodo(todo) { refresh() } }
    }
    val del = el<HTMLButtonElement>("button", "btn btnDanger") {
        textContent = "Delete"
        onclick = {
            todo.id?.let { id -> deleteTodo(id) { refresh() } }
        }
    }

    // Scheduling edit UI (start + duration) inside expanded actions
    val scheduleWrapper = el<HTMLDivElement>("div", "scheduleWrapper")
    val scheduleRow = el<HTMLDivElement>("div", "scheduleRow")
    // Replaced native datetime-local with separate date + time selects
    val dateInput = el<HTMLInputElement>("input", "textInput scheduleDateInput") {
        type = "date"
        placeholder = "Date"
        todo.startAtEpochMillis?.let {
            value = parseMillisToDateTime(it).date
        }
    }
    val hourSelect = el<HTMLSelectElement>("select", "textInput scheduleHourSelect") {
        (0..23).forEach { h ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = h.pad2()
            opt.textContent = h.pad2()
            appendChild(opt)
        }
        value = todo.startAtEpochMillis?.let { parseMillisToDateTime(it).hour } ?: ""
    }
    val minuteSelect = el<HTMLSelectElement>("select", "textInput scheduleMinuteSelect") {
        (0..55 step 5).forEach { m ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = m.pad2()
            opt.textContent = m.pad2()
            appendChild(opt)
        }
        value = todo.startAtEpochMillis?.let { parseMillisToDateTime(it).minute } ?: ""
    }
    val nowBtn = el<HTMLButtonElement>("button", "btn btnSecondary") {
        textContent = "Now"
        onclick = {
            val now = Date()
            val (date, hour, minute) = with(now) {
                val minuteRaw = getMinutes()
                val minuteRounded = (minuteRaw / 5) * 5
                Triple(
                    "${getFullYear()}-${(getMonth() + 1).pad2()}-${getDate().pad2()}",
                    getHours().pad2(),
                    minuteRounded.pad2()
                )
            }
            dateInput.value = date
            hourSelect.value = hour
            minuteSelect.value = minute
        }
    }
    val clearBtn = el<HTMLButtonElement>("button", "btn btnSecondary") {
        textContent = "Clear"
        onclick = {
            dateInput.value = ""
            hourSelect.value = ""
            minuteSelect.value = ""
        }
    }
    val durationInput = el<HTMLInputElement>("input", "textInput scheduleDurationInput") {
        type = "number"
        min = "0"
        placeholder = "Duration (min)"
        todo.durationMillis?.let { value = (it / 60000L).toString() }
    }
    val saveBtn = el<HTMLButtonElement>("button", "btn btnPrimary") {
        textContent = "Save schedule"
        onclick = {
            todo.id?.let { id ->
                val startAtMs = listOf(dateInput.value, hourSelect.value, minuteSelect.value)
                    .takeIf { inputs -> inputs.all { it.trim().isNotEmpty() } }
                    ?.let { "${dateInput.value.trim()}T${hourSelect.value.trim()}:${minuteSelect.value.trim()}" }
                    ?.let { Date(it).getTime().toLong() }

                val durationMs = durationInput.value.trim()
                    .takeIf { it.isNotEmpty() }
                    ?.toLongOrNull()
                    ?.times(60000L)

                patchSchedule(id, startAtMs, durationMs) { refresh() }
            }
        }
    }
    scheduleRow.appendChild(dateInput)
    scheduleRow.appendChild(hourSelect)
    scheduleRow.appendChild(minuteSelect)
    scheduleRow.appendChild(nowBtn)
    scheduleRow.appendChild(clearBtn)
    scheduleRow.appendChild(durationInput)
    scheduleRow.appendChild(saveBtn)
    scheduleWrapper.appendChild(scheduleRow)
    actions.appendChild(toggleBtn)
    actions.appendChild(del)
    actions.appendChild(scheduleWrapper)

    fun toggleExpanded() {
        val expanded = li.classList.toggle("expanded")
        summary.setAttribute("aria-expanded", expanded.toString())
    }
    summary.onclick = { toggleExpanded() }
    summary.onkeydown = { e ->
        if (e.key == "Enter" || e.key == " ") {
            e.preventDefault()
            toggleExpanded()
        }
    }

    li.appendChild(summary)
    if (progressContainer != null) li.appendChild(progressContainer)
    li.appendChild(actions)
    return li
}

private fun fetchTodos(done: (List<Todo>) -> Unit) {
    authedFetch(API_BASE)
        .then { resp ->
            when {
                resp.status == 401.toShort() -> {
                    val root = (document.getElementById("app") ?: document.body!!) as HTMLElement
                    logout(root)
                    null
                }
                !resp.ok -> throw Throwable("HTTP ${resp.status}")
                else -> resp.text()
            }
        }
        .then { textOrNull ->
            if (textOrNull != null) {
                val text = textOrNull as String
                console.log("Received todos: $text")
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

private fun patchSchedule(id: Long, startAtEpochMillis: Long?, durationMillis: Long?, done: () -> Unit) {
    val updateRequest = UpdateTodoRequest(startAtEpochMillis = startAtEpochMillis, durationMillis = durationMillis)
    val body = Json.encodeToString(updateRequest)
    console.log("PATCH /api/todos/$id with body: $body")
    val request = RequestInit(
        method = "PATCH",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    authedFetch("${API_BASE}/$id", request).then { done() }
}

private fun clearProgressIntervals() {
    progressIntervals.forEach { window.clearInterval(it) }
    progressIntervals.clear()
}
