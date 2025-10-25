package io.holonaut.helloserver.todo

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TodoDataSeeder {
    @Bean
    fun seedTodos(repo: TodoRepository) = CommandLineRunner {
        if (repo.count() == 0L) {
            repo.saveAll(
                listOf(
                    TodoEntity(title = "Try the new Todo app", completed = false),
                    TodoEntity(title = "Mark a todo as done", completed = true),
                )
            )
        }
    }
}
