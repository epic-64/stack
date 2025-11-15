# stack2

## Compile and run server
Remember to rerun this every time server code changes.
```bash
./gradlew :server:bootRun
```

## Build JS (dev)
Builds the JS client once. Run this after making changes:
```bash
./gradlew :js-client:jsBrowserDevelopmentWebpack
```

## Watch JS (auto-rebuild)
Watches for changes and automatically rebuilds:
```bash
./gradlew :js-client:jsBrowserDevelopmentWebpack --continuous
```

## Fix JS
If changes in Kotlin code do not show up in JS (due to Gradle caching), run this:
```bash
./gradlew :js-client:clean :js-client:jsBrowserDevelopmentWebpack --rerun-tasks
```

## Build JS (prod)
```bash
./gradlew js-client:jsBrowserProductionWebpack
```

## Start frontend
Serve this file:
[index.html](frontend/index.html)