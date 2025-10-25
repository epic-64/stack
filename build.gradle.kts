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
