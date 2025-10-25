// Root-level Gradle build file
// Adds convenience tasks to run the backend and watch/rebuild the JS client.

// This task simply delegates to the hello-server module's bootRun task provided by the Spring Boot plugin.
tasks.register("startServer") {
    group = "application"
    description = "Starts the Spring Boot server in :hello-server via bootRun."
    dependsOn(":hello-server:bootRun")
}

// Watches the js-client module and rebuilds the development bundle on file changes.
// Use Gradle's continuous mode to keep watching: `./gradlew watchJs --continuous`
tasks.register("watchJs") {
    group = "application"
    description = "Watches :js-client sources and rebuilds frontend/gen/app.js on changes."
    dependsOn(":js-client:jsBrowserDevelopmentWebpack")
}

// Aggregates ALL tests in all subprojects (JVM and JS) into a single entry point.
// It picks up conventional test task names such as `test`, `jsNodeTest`, `jsBrowserTest`, etc.
// Usage: ./gradlew allTests
tasks.register("allTests") {
    group = "verification"
    description = "Runs all unit tests in all subprojects (JVM and JS)."

    // Depend on any task named exactly 'test' or ending with 'Test' in every subproject.
    // TaskCollection is lazy, so this will also pick up tasks added later during evaluation.
    subprojects.forEach { sub ->
        dependsOn(
            sub.tasks.matching { t ->
                val n = t.name
                n == "test" || n.endsWith("Test")
            }
        )
    }
}
