import org.example.toolplugin.RunAt

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("org.example.tool")
}

kotlin {
    jvmToolchain(17)
}

tool {
    runAt = RunAt.AFTER_COMPILATION
    output = file("api.txt")
}
