plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlin.analysis.api)
    implementation(libs.clikt)
    testImplementation(libs.kotlin.test.junit5)
}

application {
    mainClass = "org.example.tool.MainKt"
}

tasks.test {
    useJUnitPlatform()
}
