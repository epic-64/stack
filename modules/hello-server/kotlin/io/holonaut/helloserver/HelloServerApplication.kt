package io.holonaut.helloserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelloServerApplication

fun main(args: Array<String>) {
    runApplication<HelloServerApplication>(*args)
}
