// Root-level Gradle build file
// Adds a convenience task to start the Spring Boot server from the command line.

// This task simply delegates to the hello-server module's bootRun task provided by the Spring Boot plugin.
tasks.register("startServer") {
    group = "application"
    description = "Starts the Spring Boot server in :hello-server via bootRun."
    dependsOn(":hello-server:bootRun")
}
