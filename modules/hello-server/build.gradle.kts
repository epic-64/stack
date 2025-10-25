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
}

// Spring Boot creates a bootJar by default. No special configuration needed for this simple module.
