import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
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
}

// Put the webpack outputs into the root-level `frontend/` directory so it can be served as a static site.
// We configure both development and production webpack tasks.

tasks.named<KotlinWebpack>("browserDevelopmentWebpack").configure {
    outputDirectory = file("${rootDir}/frontend")
}

tasks.named<KotlinWebpack>("browserProductionWebpack").configure {
    outputDirectory = file("${rootDir}/frontend")
}

// Ensure a regular `build` creates a bundle in `frontend/` as well (prod optimized).
tasks.named("build").configure {
    dependsOn(tasks.named("browserProductionWebpack"))
}
