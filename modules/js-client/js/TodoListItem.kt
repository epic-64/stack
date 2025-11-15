import io.holonaut.shared.Todo
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
    
    // First row: Date/time text input with ISO date display
    val dateTimeRow = el<HTMLDivElement>("div", "scheduleRow")
    val dateTimeInput = el<HTMLInputElement>("input", "textInput scheduleDateTimeInput") {
        type = "text"
        placeholder = "Date and time (e.g., now, yesterday, tomorrow, 2025-12-25, 2025-12-25 15:00)"
        value = todo.startAtEpochMillis?.let {
            formatDateWithTimezone(Date(it.toDouble()))
        } ?: "now"
    }
    val isoDisplayContainer = el<HTMLDivElement>("div", "isoDateDisplay")
    val isoDisplayText = el<HTMLSpanElement>("span", "isoDateText") {
        textContent = ""
    }
    isoDisplayContainer.appendChild(isoDisplayText)

    fun updateIsoDisplay() {
        val input = dateTimeInput.value
        if (input.trim().isEmpty()) {
            isoDisplayContainer.className = "isoDateDisplay"
            isoDisplayText.textContent = ""
            return
        }

        when (val result = parseUserDateTimeInput(input)) {
            is DateParseResult.Success -> {
                isoDisplayContainer.className = "isoDateDisplay valid"
                isoDisplayText.textContent = result.isoDateWithTz
            }
            is DateParseResult.Error -> {
                isoDisplayContainer.className = "isoDateDisplay invalid"
                isoDisplayText.textContent = result.message
            }
        }
    }

    dateTimeInput.oninput = { updateIsoDisplay() }

    val nowBtn = el<HTMLButtonElement>("button", "btn btnSecondary") {
        textContent = "Now"
        onclick = {
            dateTimeInput.value = "now"
            updateIsoDisplay()
        }
    }
    val clearBtn = el<HTMLButtonElement>("button", "btn btnSecondary") {
        textContent = "Clear"
        onclick = {
            dateTimeInput.value = ""
            updateIsoDisplay()
        }
    }
    
    dateTimeRow.appendChild(dateTimeInput)
    dateTimeRow.appendChild(nowBtn)
    dateTimeRow.appendChild(clearBtn)
    dateTimeRow.appendChild(isoDisplayContainer)

    // Initialize ISO display if there's an existing date
    updateIsoDisplay()

    // Second row: Duration input
    val durationRow = el<HTMLDivElement>("div", "scheduleRow")
    val durationInput = el<HTMLInputElement>("input", "textInput scheduleDurationInput") {
        type = "text"
        placeholder = "Duration (e.g., 2w 1d 3h)"
        todo.durationText?.let { value = it }
    }
    val saveBtn = el<HTMLButtonElement>("button", "btn btnPrimary") {
        textContent = "Save schedule"
        onclick = {
            todo.id?.let { id ->
                val startAtMs = if (dateTimeInput.value.trim().isNotEmpty()) {
                    when (val parseResult = parseUserDateTimeInput(dateTimeInput.value)) {
                        is DateParseResult.Success -> Date(parseResult.isoDateWithTz).getTime().toLong()
                        is DateParseResult.Error -> null
                    }
                } else {
                    null
                }

                val durationText = durationInput.value.trim()
                    .takeIf { it.isNotEmpty() }

                patchSchedule(id, startAtMs, durationText) { refresh() }
            }
        }
    }
    
    durationRow.appendChild(durationInput)
    durationRow.appendChild(saveBtn)
    
    scheduleWrapper.appendChild(dateTimeRow)
    scheduleWrapper.appendChild(durationRow)

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
