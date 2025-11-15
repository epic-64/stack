# stack2

## Compile and run server
Remember to rerun this every time server code changes.
```bash
./gradlew startServer
```

## Build JS (dev)
Builds the JS client once. Run this after making changes:
```bash
./gradlew devJs
```

## Watch JS (auto-rebuild)
Watches for changes and automatically rebuilds:
```bash
./gradlew watchJs --continuous
```
Note: You still need to manually refresh the browser after each rebuild.

## Fix JS
If changes in Kotlin code do not show up in JS (due to Gradle caching), run this:
```bash
./gradlew fixJs --rerun-tasks
```

## Build JS (prod)
```bash
./gradlew prodJs
```

## Start frontend
Serve this file:
[index.html](frontend/index.html)