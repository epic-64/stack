# stack2

## Compile and run server
Remember to rerun this every time server code changes.
```bash
./gradlew startServer
```

## Build JS (dev)
```bash
./gradlew devJs --continuous
```

## Fix JS
If changes in Kotlin code do not show up in JS, run this.
```bash
./gradlew :js-client:clean :js-client:jsBrowserDevelopmentWebpack --rerun-tasks
```

## Build JS (prod)
```bash
./gradlew prodJs
```

## Start frontend
Serve this file:
[index.html](frontend/index.html)