# stack2

This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).

This project follows the suggested multi-module setup and consists of the `app`, `utils`, and `hello-server` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).

## What is buildSrc?

Gradle has a special top‑level directory called `buildSrc/`. If it exists in your project, Gradle will treat it as a standalone build that is compiled first, and everything produced there (plugins, tasks, utilities) is placed on the classpath of your main build automatically. This lets you share build logic across all subprojects without publishing anything to an external repository.

How it works in this project:
- A reusable convention plugin is defined at `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
- Because it’s a precompiled script plugin and the file declares a package `buildsrc.convention`, its plugin id becomes `buildsrc.convention.kotlin-jvm`.
- Subprojects apply it like this:
  - `id("buildsrc.convention.kotlin-jvm")`
- The plugin applies the Kotlin JVM plugin, sets the Java toolchain to 21, and configures JUnit Platform and test logging for all `Test` tasks.

Key properties of `buildSrc`:
- Auto-compiled and on the classpath: Any code here is built first and available to the rest of the build without extra configuration.
- Kotlin/Java supported: You can write regular Kotlin/Java code, custom tasks, and precompiled script plugins.
- Fast iteration: Changes to `buildSrc` trigger its recompilation on the next build; no publishing is required.

When to use it:
- You want to keep build logic close to the build and versioned with the repository.
- You have multiple subprojects that should share the same Gradle configuration (conventions).

Alternatives:
- Convention plugins in a separate included build (often named `build-logic/`). This is more scalable for large repos, but a bit more setup. For many projects, `buildSrc` is perfectly fine.

References:
- Gradle docs: Precompiled Script Plugins and `buildSrc` directory


## Hello server module

A minimal Spring Boot server lives in the `hello-server` module with a single route.

How to run:
- From the project root (convenience task): `./gradlew startServer`
- Or directly via the module task: `./gradlew :hello-server:bootRun`

Then in another terminal:
- `curl http://localhost:8080/hello`

You should see:
- `Hello, world!`

## Kotlin/JS frontend

A simple Kotlin/JS module lives in `modules/js-client`. It compiles to a single bundle `app.js` that is written to the root-level `frontend/` directory alongside an `index.html` file.

How to build the JS bundle:
- `./gradlew :js-client:jsBrowserProductionWebpack`
  - The output `frontend/app.js` will be generated.

Open the frontend:
- Open `frontend/index.html` in your browser (double-click it or serve the folder via any static HTTP server).
- You should see the text replaced to: `Hello from Kotlin/JS!`.
