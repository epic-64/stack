import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
}

kotlin {
    // Ensure a full JDK with compiler is provisioned for the JVM target
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    jvm()
    js(IR) {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            kotlin.srcDirs("common")
            resources.srcDirs("common-resources")
            dependencies {
                implementation(libs.kotlinxSerialization)
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            kotlin.srcDirs("jvm-test")
            dependencies {
                implementation(libs.kotestRunnerJunit5)
                implementation(kotlin("reflect"))
            }
        }
        val jsMain by getting
        val jsTest by getting
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", true)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", true)
}
