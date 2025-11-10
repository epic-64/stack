We are building a todolist application in a Kotlin Multiplatform project.
All modules live in the modules directory.
IN gradle commands, omit the modules prefix, e.g.
```
./gradlew :server:bootRun
./gradlew :js-client:jsBrowserDevelopmentWebpack
```

Please, respect the author. You should not touch any code that is not directly related to your task.
Leave the coding style alone. Leave comments alone. Leave formatting alone.

Excluded files, by company policy - do not try to open or modify them:
- application.properties

We are on a local dev environment.
Run gradlew in daemon mode by default to improve performance.

In frontend kotlin code, there is a 100% chance if you type `${'$'}`, it's bullshit.
Never do that.

With that out of the way, have fun coding!