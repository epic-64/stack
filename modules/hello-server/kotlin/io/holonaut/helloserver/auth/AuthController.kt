package io.holonaut.helloserver.auth

import io.holonaut.helloserver.security.AppUserDetails
import io.holonaut.helloserver.user.UserEntity
import io.holonaut.helloserver.user.UserRepository
import io.holonaut.shared.User
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val users: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    data class RegisterRequest(val username: String, val password: String)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody req: RegisterRequest): User {
        if (req.username.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "username must not be blank")
        if (req.password.length < 4) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "password too short")
        if (users.existsByUsername(req.username)) throw ResponseStatusException(HttpStatus.CONFLICT, "username already exists")
        val entity = UserEntity(username = req.username.trim(), passwordHash = passwordEncoder.encode(req.password))
        return users.save(entity).toDto()
    }

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: AppUserDetails): User = principal.user.toDto()
}

private fun UserEntity.toDto() = User(
    id = id,
    username = username,
    teamIds = teams.mapNotNull { it.id }
)

