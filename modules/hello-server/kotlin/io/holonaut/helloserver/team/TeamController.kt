package io.holonaut.helloserver.team

import io.holonaut.helloserver.security.AppUserDetails
import io.holonaut.helloserver.user.UserRepository
import io.holonaut.shared.Team
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/teams")
class TeamController(
    private val teams: TeamRepository,
    private val users: UserRepository,
) {
    data class CreateTeamRequest(val name: String)
    data class AddMemberRequest(val username: String)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreateTeamRequest, @AuthenticationPrincipal principal: AppUserDetails): Team {
        if (req.name.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        if (teams.existsByName(req.name)) throw ResponseStatusException(HttpStatus.CONFLICT, "team name exists")
        val entity = TeamEntity(name = req.name.trim())
        entity.members.add(principal.user)
        principal.user.teams.add(entity)
        return teams.save(entity).toDto()
    }

    @GetMapping
    fun myTeams(@AuthenticationPrincipal principal: AppUserDetails): List<Team> = principal.user.teams
        .sortedBy { it.id }
        .map { it.toDto() }

    @PostMapping("/{id}/members")
    fun addMember(@PathVariable id: Long, @RequestBody req: AddMemberRequest, @AuthenticationPrincipal principal: AppUserDetails): Team {
        val team = teams.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Team $id not found") }
        if (team.members.none { it.id == principal.user.id }) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member")
        val user = users.findByUsername(req.username).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        team.members.add(user)
        user.teams.add(team)
        return teams.save(team).toDto()
    }
}

private fun TeamEntity.toDto() = Team(
    id = id,
    name = name,
    createdAtEpochMillis = createdAt?.toEpochMilli(),
    updatedAtEpochMillis = updatedAt?.toEpochMilli(),
)
