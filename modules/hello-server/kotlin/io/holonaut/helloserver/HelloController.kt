package io.holonaut.helloserver

import io.holonaut.shared.Greeting
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello(): Greeting {
        // Spring Boot will automatically serialize this data class to JSON using Jackson.
        return Greeting(message = "Hello, world!", timestampMillis = System.currentTimeMillis())
    }
}
