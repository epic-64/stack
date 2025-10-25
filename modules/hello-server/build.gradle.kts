plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSpring)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

dependencies {
    implementation(libs.springBootStarterWeb)
    implementation(project(":shared"))
    testImplementation(libs.springBootStarterTest)
}

// Spring Boot creates a bootJar by default. No special configuration needed for this simple module.
