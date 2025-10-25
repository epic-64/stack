import io.holonaut.shared.Todo
import io.holonaut.shared.User
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise
import kotlin.js.json

private const val API_BASE = "http://localhost:8080/api/todos"
private const val AUTH_BASE = "http://localhost:8080/api/auth"

// Keep auth state (Basic auth header) in memory; persisted in localStorage as raw base64 value without prefix.
private var basicAuthB64: String? = null

@Serializable
private data class CreateTodoRequest(val title: String, val completed: Boolean? = null)

@Serializable
private data class UpdateTodoRequest(val title: String? = null, val completed: Boolean? = null)

fun main() {
    val root = (document.getElementById("app") ?: document.body!!) as HTMLElement
    root.innerHTML = "" // clear placeholder
    root.className = Css.appContainer

    // Try auto-login using stored credentials, then either show app or auth forms.
    val stored = window.localStorage.getItem("auth")
    if (stored != null && stored.isNotBlank()) {
        basicAuthB64 = stored
        fetchMe(
            ok = { user -> renderApp(root, user) },
            unauthorized = { renderAuth(root) }
        )
    } else {
        renderAuth(root)
    }
}

// -------------------------------------------------------------
// Auth (login / register) UI
// -------------------------------------------------------------
private fun renderAuth(root: HTMLElement) {
    root.innerHTML = ""

    val title = document.createElement("h1").apply { textContent = "Stack" }
    val msg = document.createElement("div") as HTMLDivElement
    msg.style.marginBottom = "12px"

    val container = document.createElement("div") as HTMLDivElement
    container.style.display = "flex"
    container.style.flexDirection = "column"
    container.style.asDynamic().gap = "12px"

    // Form elements shared
    val username = document.createElement("input") as HTMLInputElement
    username.placeholder = "Username"
    username.className = "textInput"
    username.autocomplete = "username"

    val password = document.createElement("input") as HTMLInputElement
    password.placeholder = "Password"
    password.type = "password"
    password.className = "textInput"
    password.autocomplete = "current-password"

    val submitBtn = document.createElement("button") as HTMLButtonElement
    submitBtn.className = "btn btnPrimary"

    val toggleLink = document.createElement("button") as HTMLButtonElement
    toggleLink.className = "btn btnSecondary"

    var mode = "login" // or "register"
    fun updateMode() {
        if (mode == "login") {
            submitBtn.textContent = "Login"
            toggleLink.textContent = "Need an account? Register"
        } else {
            submitBtn.textContent = "Create account"
            toggleLink.textContent = "Have an account? Login"
        }
        msg.textContent = ""
        username.focus()
    }

    fun setError(text: String) {
        msg.textContent = text
        msg.className = Css.error
    }

    submitBtn.onclick = {
        val u = username.value.trim()
        val p = password.value
        if (u.isEmpty() || p.isEmpty()) {
            setError("Username & password required")
        } else {
            if (mode == "register") doRegister(u, p, root, ::renderApp, ::setError) else doLogin(u, p, root, ::renderApp, ::setError)
        }
    }
    password.onkeypress = { e -> if (e.key == "Enter") submitBtn.click() }

    toggleLink.onclick = {
        mode = if (mode == "login") "register" else "login"
        updateMode()
    }

    updateMode()

    container.appendChild(username)
    container.appendChild(password)
    container.appendChild(submitBtn)
    container.appendChild(toggleLink)

    root.appendChild(title)
    root.appendChild(msg)
    root.appendChild(container)
}

private fun doLogin(username: String, password: String, root: HTMLElement, onSuccess: (HTMLElement, User) -> Unit, onError: (String) -> Unit) {
    basicAuthB64 = window.btoa("$username:$password")
    window.localStorage.setItem("auth", basicAuthB64!!)
    fetchMe(
        ok = { user -> onSuccess(root, user) },
        unauthorized = {
            onError("Invalid credentials")
            clearAuth()
        },
        failure = { onError(it.message ?: "Login failed") }
    )
}

private fun doRegister(username: String, password: String, root: HTMLElement, onSuccess: (HTMLElement, User) -> Unit, onError: (String) -> Unit) {
    val body = Json.encodeToString(mapOf("username" to username, "password" to password))
    val init = RequestInit(
        method = "POST",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    window.fetch("$AUTH_BASE/register", init)
        .then { resp ->
            if (resp.status == 201.toShort()) {
                // Auto-login after successful registration
                doLogin(username, password, root, onSuccess, onError)
            } else {
                resp.text().then { txt ->
                    onError("Register failed: ${'$'}{resp.status} ${'$'}txt")
                }
            }
        }
        .catch { err -> onError("Register failed: ${'$'}err") }
}

private fun fetchMe(ok: (User) -> Unit, unauthorized: () -> Unit, failure: (Throwable) -> Unit = {}) {
    authedFetch("$AUTH_BASE/me")
        .then { resp ->
            if (resp.status == 401.toShort()) {
                unauthorized(); null
            } else if (!resp.ok) {
                throw Throwable("/me failed: ${'$'}{resp.status}")
            } else resp.text()
        }
        .then { anyText ->
            if (anyText != null) {
                val text = anyText as String
                ok(Json.decodeFromString(text))
            }
        }
        .catch { e -> failure(e) }
}

private fun clearAuth() {
    basicAuthB64 = null
    window.localStorage.removeItem("auth")
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
        clearAuth()
        renderAuth(root)
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

    refresh() // initial load
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
    authedFetch(API_BASE)
        .then { resp ->
            if (resp.status == 401.toShort()) {
                // Session expired or logged out elsewhere – force re-auth
                clearAuth()
                val root = (document.getElementById("app") ?: document.body!!) as HTMLElement
                renderAuth(root)
                null
            } else if (!resp.ok) throw Throwable("HTTP ${'$'}{resp.status}") else resp.text()
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

// Wrapper adding Authorization header when available.
private fun authedFetch(url: String, init: RequestInit = RequestInit()): Promise<Response> {
    val headers = js("Object.assign({}, init.headers || {})")
    val auth = basicAuthB64
    if (auth != null) js("headers['Authorization'] = 'Basic ' + auth")
    js("init.headers = headers")
    return window.fetch(url, init)
}
