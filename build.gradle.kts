// Root-level Gradle build file
// Adds convenience tasks to run the backend and watch/rebuild the JS client.

// This task simply delegates to the server module's bootRun task provided by the Spring Boot plugin.
tasks.register("startServer") {
    group = "application"
    description = "Starts the Spring Boot server in :server via bootRun."
    dependsOn(":server:bootRun")
}

// Use Gradle's continuous mode to keep watching: `./gradlew devJs --continuous`
tasks.register("devJs") {
    group = "application"
    description = "Builds the JS client for development (one-time build)."
    dependsOn(":shared:compileKotlinJs", ":js-client:jsBrowserDevelopmentWebpack")
}

tasks.register("watchJs") {
    group = "application"
    description = "Watches sources and rebuilds JS on changes. Run with: ./gradlew watchJs --continuous"
    dependsOn(":shared:compileKotlinJs", ":js-client:jsBrowserDevelopmentWebpack")
}

tasks.register("prodJs") {
    group = "application"
    description = "Builds the production version of the JS client to frontend/gen/app.js."
    dependsOn(":js-client:jsBrowserProductionWebpack")
}

tasks.register("fixJs") {
    group = "application"
    description = "Cleans and rebuilds the JS client with --rerun-tasks to force a fresh build."
    dependsOn(":js-client:clean", ":js-client:jsBrowserDevelopmentWebpack")
    tasks.findByPath(":js-client:jsBrowserDevelopmentWebpack")?.mustRunAfter(":js-client:clean")
}

