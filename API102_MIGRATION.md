# API 102 migration

This branch uses the modern libxposed API 102 entry point.

## Build

Use JDK 17 and Android SDK Platform 37. From this directory run:

```text
gradlew.bat assembleDebug
```

The project depends on `io.github.libxposed:api:102.0.0` for compilation and packages the API 102 service library for the settings app. The framework supplies the API classes to the injected module process.

The arm64-v8a and armeabi-v7a native `UnityFPSUnlocker` libraries are included under `app/src/main/libs/`.

Install the resulting APK in an API 102 capable framework such as a current LSPosed/Vector build, enable the module for the target games, and reboot or restart the target process.
