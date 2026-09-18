plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("tool") {
            id = "org.example.tool"
            implementationClass = "org.example.toolplugin.ToolPlugin"
        }
    }
}
