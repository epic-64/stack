package io.holonaut.helloserver.todo

import io.holonaut.shared.Todo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/todos")
class TodoController(
    private val repo: TodoRepository,
    private val teamRepo: io.holonaut.helloserver.team.TeamRepository,
) {

    @GetMapping
    fun list(@org.springframework.security.core.annotation.AuthenticationPrincipal principal: io.holonaut.helloserver.security.AppUserDetails): List<Todo> {
        val user = principal.user
        val teamIds = user.teams.mapNotNull { it.id }
        val entities = if (teamIds.isEmpty()) {
            repo.findByCreatedBy_Id(user.id!!)
        } else {
            repo.findAccessible(user.id!!, teamIds)
        }
        return entities.sortedBy { it.id }.map { it.toDto() }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long, @org.springframework.security.core.annotation.AuthenticationPrincipal principal: io.holonaut.helloserver.security.AppUserDetails): Todo {
        val user = principal.user
        val entity = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found") }
        if (!entity.isAccessibleBy(user)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found")
        return entity.toDto()
    }

    data class CreateTodoRequest(val title: String, val completed: Boolean? = null, val teamIds: List<Long>? = null)
    data class UpdateTodoRequest(val title: String? = null, val completed: Boolean? = null, val teamIds: List<Long>? = null)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreateTodoRequest, @org.springframework.security.core.annotation.AuthenticationPrincipal principal: io.holonaut.helloserver.security.AppUserDetails): Todo {
        if (req.title.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank")
        val user = principal.user
        val entity = TodoEntity(title = req.title.trim(), completed = req.completed ?: false, createdBy = user)
        val teams = loadAndValidateTeams(req.teamIds, user)
        entity.teams.addAll(teams)
        return repo.save(entity).toDto()
    }

    @PutMapping("/{id}")
    fun replace(@PathVariable id: Long, @RequestBody req: CreateTodoRequest, @org.springframework.security.core.annotation.AuthenticationPrincipal principal: io.holonaut.helloserver.security.AppUserDetails): Todo {
        if (req.title.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank")
        val user = principal.user
        val entity = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found") }
        if (!entity.isAccessibleBy(user)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found")
        entity.title = req.title.trim()
        entity.completed = req.completed ?: false
        entity.teams.clear()
        entity.teams.addAll(loadAndValidateTeams(req.teamIds, user))
        return repo.save(entity).toDto()
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: UpdateTodoRequest, @org.springframework.security.core.annotation.AuthenticationPrincipal principal: io.holonaut.helloserver.security.AppUserDetails): Todo {
        val user = principal.user
        val entity = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found") }
        if (!entity.isAccessibleBy(user)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found")
        req.title?.let { entity.title = it.trim() }
        req.completed?.let { entity.completed = it }
        if (req.teamIds != null) {
            entity.teams.clear()
            entity.teams.addAll(loadAndValidateTeams(req.teamIds, user))
        }
        return repo.save(entity).toDto()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long, @org.springframework.security.core.annotation.AuthenticationPrincipal principal: io.holonaut.helloserver.security.AppUserDetails) {
        val user = principal.user
        val entity = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found") }
        if (!entity.isAccessibleBy(user)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo $id not found")
        repo.delete(entity)
    }

    private fun loadAndValidateTeams(ids: List<Long>?, user: io.holonaut.helloserver.user.UserEntity): List<io.holonaut.helloserver.team.TeamEntity> {
        if (ids == null) return emptyList()
        if (ids.isEmpty()) return emptyList()
        val userTeamIds = user.teams.mapNotNull { it.id }.toSet()
        if (!userTeamIds.containsAll(ids)) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot assign to teams you do not belong to")
        val teams = teamRepo.findAllById(ids)
        if (teams.count() != ids.size) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Some teams not found")
        return teams
    }
}

private fun TodoEntity.isAccessibleBy(user: io.holonaut.helloserver.user.UserEntity): Boolean {
    val userId = user.id
    if (userId != null && this.createdBy?.id == userId) return true
    val userTeamIds = user.teams.mapNotNull { it.id }.toSet()
    return teams.any { it.id != null && userTeamIds.contains(it.id) }
}

private fun TodoEntity.toDto(): Todo = Todo(
    id = id,
    title = title,
    completed = completed,
    createdAtEpochMillis = createdAt?.toEpochMilli(),
    updatedAtEpochMillis = updatedAt?.toEpochMilli(),
    teamIds = teams.mapNotNull { it.id }.sorted(),
)
