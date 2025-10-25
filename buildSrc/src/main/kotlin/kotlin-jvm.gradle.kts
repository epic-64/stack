// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin in JVM projects.
    kotlin("jvm")
}

kotlin {
    // Use a specific Java version and vendor; with the Foojay resolver this ensures a full JDK is provisioned.
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

// Configure a simplified source layout for JVM modules:
// - Place production sources in "kotlin/" at the module root (instead of "src/main/kotlin")
// - Place test sources in "test/" at the module root (instead of "src/test/kotlin")
sourceSets {
    val main by getting {
        kotlin.srcDirs("kotlin")
        resources.srcDirs("resources")
    }
    val test by getting {
        kotlin.srcDirs("test")
        resources.srcDirs("test-resources")
    }
}

tasks.withType<Test>().configureEach {
    // Configure all test Gradle tasks to use JUnitPlatform.
    useJUnitPlatform()

    // Log information about all test results, not only the failed ones.
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}
