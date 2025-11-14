import io.holonaut.shared.Todo
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*
import kotlin.js.Date
import kotlin.math.roundToInt

private var progressIntervals: MutableList<Int> = mutableListOf()

fun clearProgressIntervals() {
    progressIntervals.forEach { window.clearInterval(it) }
    progressIntervals.clear()
}

fun renderTodosInto(list: HTMLUListElement, todos: List<Todo>, refresh: () -> Unit) {
    clearProgressIntervals()
    list.innerHTML = ""
    todos.forEach { todo ->
        list.appendChild(buildTodoListItem(todo, refresh))
    }
}

fun buildTodoListItem(todo: Todo, refresh: () -> Unit): HTMLLIElement {
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
    
    // Date and time inputs
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
        type = "text"
        placeholder = "Duration (e.g., 2w 1d 3h)"
        todo.durationText?.let { value = it }
    }
    val saveBtn = el<HTMLButtonElement>("button", "btn btnPrimary") {
        textContent = "Save schedule"
        onclick = {
            todo.id?.let { id ->
                val startAtMs = listOf(dateInput.value, hourSelect.value, minuteSelect.value)
                    .takeIf { inputs -> inputs.all { it.trim().isNotEmpty() } }
                    ?.let { "${dateInput.value.trim()}T${hourSelect.value.trim()}:${minuteSelect.value.trim()}" }
                    ?.let { Date(it).getTime().toLong() }

                val durationText = durationInput.value.trim()
                    .takeIf { it.isNotEmpty() }

                patchSchedule(id, startAtMs, durationText) { refresh() }
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
