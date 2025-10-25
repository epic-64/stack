package io.holonaut.helloserver.todo

import io.holonaut.shared.Todo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/todos")
class TodoController(
    private val repo: TodoRepository
) {

    @GetMapping
    fun list(): List<Todo> = repo.findAll()
        .sortedBy { it.id }
        .map { it.toDto() }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Todo = repo.findById(id)
        .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found") }
        .toDto()

    data class CreateTodoRequest(val title: String, val completed: Boolean? = null)
    data class UpdateTodoRequest(val title: String? = null, val completed: Boolean? = null)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreateTodoRequest): Todo {
        if (req.title.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank")
        val entity = TodoEntity(title = req.title.trim(), completed = req.completed ?: false)
        return repo.save(entity).toDto()
    }

    @PutMapping("/{id}")
    fun replace(@PathVariable id: Long, @RequestBody req: CreateTodoRequest): Todo {
        if (req.title.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank")
        val entity = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found") }
        entity.title = req.title.trim()
        entity.completed = req.completed ?: false
        return repo.save(entity).toDto()
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: UpdateTodoRequest): Todo {
        val entity = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found") }
        req.title?.let { entity.title = it.trim() }
        req.completed?.let { entity.completed = it }
        return repo.save(entity).toDto()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!repo.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found")
        repo.deleteById(id)
    }
}

private fun TodoEntity.toDto(): Todo = Todo(
    id = id,
    title = title,
    completed = completed,
    createdAtEpochMillis = createdAt?.toEpochMilli(),
    updatedAtEpochMillis = updatedAt?.toEpochMilli(),
)
