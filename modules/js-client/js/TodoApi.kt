import io.holonaut.shared.Todo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import kotlin.js.json

private const val API_BASE = "http://localhost:8080/api/todos"

@Serializable
data class CreateTodoRequest(
    val title: String,
    val completed: Boolean? = null
)

@Serializable
data class UpdateTodoRequest(
    val title: String? = null,
    val completed: Boolean? = null,
    val startAtEpochMillis: Long? = null,
    val durationText: String? = null,
    val durationMillis: Long? = null,
)

fun fetchTodos(done: (List<Todo>) -> Unit) {
    authedFetch(API_BASE)
        .then { resp ->
            when {
                resp.status == 401.toShort() -> {
                    val root = (kotlinx.browser.document.getElementById("app") ?: kotlinx.browser.document.body!!) as org.w3c.dom.HTMLElement
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

fun createTodo(title: String, done: () -> Unit) {
    val body = Json.encodeToString(CreateTodoRequest(title = title))
    val request = RequestInit(
        method = "POST",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    authedFetch(API_BASE, request).then { done() }
}

fun toggleTodo(todo: Todo, done: () -> Unit) {
    val body = Json.encodeToString(UpdateTodoRequest(completed = !todo.completed))
    val request = RequestInit(
        method = "PATCH",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    authedFetch("${API_BASE}/${todo.id}", request).then { done() }
}

fun deleteTodo(id: Long, done: () -> Unit) {
    authedFetch("${API_BASE}/$id", RequestInit(method = "DELETE")).then { done() }
}

fun patchSchedule(id: Long, startAtEpochMillis: Long?, durationText: String?, done: () -> Unit) {
    val updateRequest = UpdateTodoRequest(startAtEpochMillis = startAtEpochMillis, durationText = durationText)
    val body = Json.encodeToString(updateRequest)
    console.log("PATCH /api/todos/$id with body: $body")
    val request = RequestInit(
        method = "PATCH",
        headers = json("Content-Type" to "application/json"),
        body = body
    )
    authedFetch("${API_BASE}/$id", request).then { done() }
}
