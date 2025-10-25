// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// Include the `app` and `utils` subprojects in the build.
// If there are changes in only one of the projects, Gradle will rebuild only the one that has changed.
// Learn more about structuring projects with Gradle - https://docs.gradle.org/8.7/userguide/multi_project_builds.html
include(":app")
include(":utils")
include(":hello-server")
include(":js-client")
include(":shared")

// Map logical project names to their new physical locations under the `modules/` directory.
project(":app").projectDir = file("modules/app")
project(":utils").projectDir = file("modules/utils")
project(":hello-server").projectDir = file("modules/hello-server")
project(":js-client").projectDir = file("modules/js-client")
project(":shared").projectDir = file("modules/shared")

rootProject.name = "stack"