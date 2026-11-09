# Analysis API quickstart

A small Gradle project for writing command-line tools on top of the [Kotlin Analysis API](https://kotl.in/analysis-api).  
Use it as a
starting point: replace the sample logic with your own analysis or code generation, and run it against any module right from the build.

## What's inside

- `tool` is the tool itself: a regular CLI application that sets up a standalone Analysis API session from command-line arguments and runs
  the actual logic. The sample implementation dumps the public API of a module.
- `tool-plugin` is a Gradle plugin that runs the tool for a module. It collects source roots, the compilation classpath and compiler
  settings, and passes them to the tool as arguments. The tool runs in a separate JVM, so the Analysis API and its dependencies never clash
  with Gradle's own classpath.
- `sandbox` is a module with the plugin applied. Put code there and see what the tool makes of it.

## Try it

```
./gradlew :sandbox:build
```

The sandbox is configured to run the tool right after compilation, and the report lands in `sandbox/api.txt`.
You can also run the tool on demand with `./gradlew :sandbox:runTool`, or call it as a plain CLI app:

```
./gradlew :tool:run --args="--module-name demo --source-root $PWD/sandbox/src/main/kotlin"
```

## Writing your own tool

Start with [`Tool.kt`](tool/src/main/kotlin/org/example/tool/Tool.kt) — the whole sample logic is there, and it's about thirty lines.
The session setup in [`AnalysisSession.kt`](tool/src/main/kotlin/org/example/tool/AnalysisSession.kt) (source roots, classpath, JDK,
language settings) usually doesn't need to change.

When the tool needs a new option, declare it in `ToolCommand` ([`Main.kt`](tool/src/main/kotlin/org/example/tool/Main.kt)) and pass it from
the plugin in `ToolArguments` ([`ToolPlugin.kt`](tool-plugin/src/main/kotlin/org/example/toolplugin/ToolPlugin.kt)).

The plugin is configured per module:

```kotlin
tool {
    runAt = RunAt.AFTER_COMPILATION // or BEFORE_COMPILATION, or MANUAL (the default)
    output = file("api.txt")        // printed to the console when not set
}
```

By default, the plugin runs the `:tool` project from this build. If your tool is published, point the `toolClasspath` configuration at its
coordinates instead.

## Versions

Dependency versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). The Analysis API is published on Maven Central as
`org.jetbrains.kotlin:kotlin-analysis-api` and is versioned together with the Kotlin compiler.  
A JDK 17+ is all you need to build; the Gradle wrapper and toolchains handle the rest.
