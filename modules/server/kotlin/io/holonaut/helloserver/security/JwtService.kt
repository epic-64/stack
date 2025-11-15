package io.holonaut.helloserver.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {
    // In production, this should come from environment variables or secure configuration
    private val secret: SecretKey = Keys.hmacShaKeyFor(
        "your-secret-key-should-be-at-least-256-bits-long-for-hs256-algorithm".toByteArray()
    )

    private val expirationMs = 86400000L // 24 hours

    fun generateToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secret)
            .compact()
    }

    fun getUsernameFromToken(token: String): String? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
            claims.payload.subject
        } catch (e: Exception) {
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}

