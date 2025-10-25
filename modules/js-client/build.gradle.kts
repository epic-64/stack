import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                // Name the produced bundle consistently. It will be written into the frontend/ dir (see tasks below).
                outputFileName = "app.js"
            }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(libs.kotlinxSerialization)
            }
        }
    }
}

// Put the webpack outputs into the root-level `frontend/` directory so it can be served as a static site.
// We configure both development and production webpack tasks.

tasks.named<KotlinWebpack>("jsBrowserDevelopmentWebpack").configure {
    outputDirectory = file("${rootDir}/frontend/gen")
}

tasks.named<KotlinWebpack>("jsBrowserProductionWebpack").configure {
    outputDirectory = file("${rootDir}/frontend/gen")
}

// Ensure a regular `build` creates a bundle in `frontend/gen/` as well (prod optimized).
tasks.named("build").configure {
    dependsOn(tasks.named("jsBrowserProductionWebpack"))
}
