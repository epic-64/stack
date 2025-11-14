import io.holonaut.shared.User
import kotlinx.browser.document
import org.w3c.dom.*

fun main() {
    val root = (document.getElementById("app") ?: document.body!!) as HTMLElement
    root.innerHTML = ""
    root.className = Css.appContainer
    initAuth(root) { user ->
        renderApp(root, user)
    }
}

fun renderApp(root: HTMLElement, user: User) {
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
