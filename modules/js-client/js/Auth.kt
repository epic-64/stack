import io.holonaut.shared.User
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise
import kotlin.js.json
import kotlin.js.JSON

private const val AUTH_BASE = "http://localhost:8080/api/auth"

// JWT token kept in memory + localStorage.
private var jwtToken: String? = null
private var lastOnAuthenticated: ((User) -> Unit)? = null

// Entry point used by Main.kt to bootstrap auth and then the app.
fun initAuth(root: HTMLElement, onAuthenticated: (User) -> Unit) {
    lastOnAuthenticated = onAuthenticated
    val stored = window.localStorage.getItem("auth")
    if (stored != null && stored.isNotBlank()) {
        jwtToken = stored
        fetchMe(
            ok = { user -> onAuthenticated(user) },
            unauthorized = { renderAuth(root, onAuthenticated) }
        )
    } else {
        renderAuth(root, onAuthenticated)
    }
}

fun logout(root: HTMLElement) {
    clearAuth()
    val cb = lastOnAuthenticated
    renderAuth(root, cb ?: { })
}

// Public fetch wrapper (used by Main.kt) injecting Authorization header when present.
fun authedFetch(url: String, init: RequestInit = RequestInit()): Promise<Response> {
    val headers = js("Object.assign({}, init.headers || {})")
    val token = jwtToken
    if (token != null) js("headers['Authorization'] = 'Bearer ' + token")
    js("init.headers = headers")
    return window.fetch(url, init)
}

// -------------------------------------------------------------
// Internal auth helpers
// -------------------------------------------------------------
private fun renderAuth(root: HTMLElement, onAuthenticated: (User) -> Unit) {
    root.innerHTML = ""

    val title = document.createElement("h1").apply { textContent = "Stack" }
    val msg = document.createElement("div") as HTMLDivElement
    msg.style.marginBottom = "12px"

    val container = document.createElement("div") as HTMLDivElement
    container.style.display = "flex"
    container.style.flexDirection = "column"
    container.style.asDynamic().gap = "12px"

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

    val toggleBtn = document.createElement("button") as HTMLButtonElement
    toggleBtn.className = "btn btnSecondary"

    var mode = "login"

    fun setError(text: String) {
        msg.textContent = text
        msg.className = Css.error
    }

    fun updateMode() {
        if (mode == "login") {
            submitBtn.textContent = "Login"
            toggleBtn.textContent = "Need an account? Register"
        } else {
            submitBtn.textContent = "Create account"
            toggleBtn.textContent = "Have an account? Login"
        }
        msg.textContent = ""
        username.focus()
    }

    submitBtn.onclick = {
        val u = username.value.trim()
        val p = password.value
        if (u.isEmpty() || p.isEmpty()) {
            setError("Username & password required")
        } else if (mode == "register") {
            doRegister(u, p, onAuthenticated, ::setError)
        } else {
            doLogin(u, p, onAuthenticated, ::setError)
        }
    }
    password.onkeypress = { e -> if (e.key == "Enter") submitBtn.click() }

    toggleBtn.onclick = {
        mode = if (mode == "login") "register" else "login"
        updateMode()
    }

    updateMode()

    container.appendChild(username)
    container.appendChild(password)
    container.appendChild(submitBtn)
    container.appendChild(toggleBtn)

    root.appendChild(title)
    root.appendChild(msg)
    root.appendChild(container)
}

private fun doLogin(username: String, password: String, onAuthenticated: (User) -> Unit, onError: (String) -> Unit) {
    val body = Json.encodeToString(mapOf("username" to username, "password" to password))
    val init = RequestInit(
        method = "POST",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    window.fetch("$AUTH_BASE/login", init)
        .then { resp ->
            if (resp.status == 200.toShort()) {
                resp.text().then { text ->
                    try {
                        val loginResponse = JSON.parse<dynamic>(text)
                        jwtToken = loginResponse.token as String
                        window.localStorage.setItem("auth", jwtToken!!)
                        val user = Json.decodeFromString<User>(JSON.stringify(loginResponse.user))
                        onAuthenticated(user)
                    } catch (e: dynamic) {
                        onError("Login failed: ${e?.toString() ?: "decode error"}")
                    }
                }
            } else {
                resp.text().then { txt -> onError("Login failed: ${resp.status} $txt") }
            }
        }
        .catch { err -> onError("Login failed: $err") }
}

private fun doRegister(username: String, password: String, onAuthenticated: (User) -> Unit, onError: (String) -> Unit) {
    val body = Json.encodeToString(mapOf("username" to username, "password" to password))
    val init = RequestInit(
        method = "POST",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    window.fetch("$AUTH_BASE/register", init)
        .then { resp ->
            if (resp.status == 201.toShort()) {
                doLogin(username, password, onAuthenticated, onError)
            } else {
                resp.text().then { txt -> onError("Register failed: ${resp.status} $txt") }
            }
        }
        .catch { err -> onError("Register failed: $err") }
}

private fun fetchMe(ok: (User) -> Unit, unauthorized: () -> Unit, failure: (Throwable) -> Unit = {}) {
    authedFetch("$AUTH_BASE/me")
        .then { resp ->
            when {
                resp.status == 401.toShort() || resp.status == 403.toShort() -> { unauthorized(); null }
                !resp.ok -> throw Throwable("/me failed: ${resp.status}")
                else -> resp.text().then { text ->
                    try {
                        ok(Json.decodeFromString(text))
                    } catch (e: dynamic) {
                        failure(Throwable(e?.toString() ?: "decode error"))
                    }
                }
            }
        }
        .catch { e -> failure(Throwable(e.toString())) }
}

private fun clearAuth() {
    jwtToken = null
    window.localStorage.removeItem("auth")
}
