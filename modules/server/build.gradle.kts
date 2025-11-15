plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSpring)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

dependencies {
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterDataJpa)
    runtimeOnly(libs.h2Database)
    implementation(project(":shared"))
    // Required by Spring for Kotlin reflection (e.g., data class parameter names)
    implementation(kotlin("reflect"))
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.kotestAssertionsCore)
    testImplementation(libs.kotestRunnerJunit5)
    implementation(libs.springBootStarterSecurity)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.jjwtApi)
    runtimeOnly(libs.jjwtImpl)
    runtimeOnly(libs.jjwtJackson)
}

// Spring Boot creates a bootJar by default. No special configuration needed for this simple module.

// Ensure the development profile (file-based H2) is active when running the app via bootRun.
// The test tasks explicitly use the in-memory profile for isolation and speed.
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    systemProperty("spring.profiles.active", "dev")
}

tasks.withType<Test> {
    // Use the in-memory DB defined in application-test.properties
    systemProperty("spring.profiles.active", "test")
    // Disable Kotest autoscan per request
    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
}
