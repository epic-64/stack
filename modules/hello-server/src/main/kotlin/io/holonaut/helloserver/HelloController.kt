package io.holonaut.helloserver

import io.holonaut.shared.Greeting
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello(): String {
        val greeting = Greeting(message = "Hello, world!", timestampMillis = System.currentTimeMillis())
        // For simplicity, we return just the message to avoid extra JSON setup.
        return greeting.message
    }
}
