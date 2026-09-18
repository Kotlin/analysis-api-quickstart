package org.example.toolplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.io.File

class ToolPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("tool", ToolExtension::class.java)
        extension.runAt.convention(RunAt.MANUAL)

        // What to run: by default the `:tool` project from this build.
        val toolClasspath = project.configurations.create("toolClasspath") {
            isCanBeConsumed = false
            defaultDependencies {
                add(project.dependencies.create(project.project(":tool")))
            }
        }

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)

            val runTool = project.tasks.register("runTool", JavaExec::class.java) {
                group = "tool"
                description = "Runs the Analysis API tool on this module's sources"
                classpath = toolClasspath
                mainClass.set("org.example.tool.MainKt")
                argumentProviders.add(
                    ToolArguments(
                        moduleName = project.name,
                        sourceRoots = project.objects.fileCollection()
                            .from(kotlin.sourceSets.named("main").map { it.kotlin.sourceDirectories }),
                        analyzedClasspath = project.objects.fileCollection()
                            .from(project.configurations.named("compileClasspath")),
                        languageVersion = kotlin.compilerOptions.languageVersion.map { it.version },
                        apiVersion = kotlin.compilerOptions.apiVersion.map { it.version },
                        jvmTarget = kotlin.compilerOptions.jvmTarget.map { it.target },
                        output = extension.output
                    )
                )
            }

            project.afterEvaluate {
                val compileKotlin = project.tasks.named("compileKotlin")
                when (extension.runAt.get()) {
                    RunAt.BEFORE_COMPILATION -> compileKotlin.configure { dependsOn(runTool) }
                    RunAt.AFTER_COMPILATION -> compileKotlin.configure { finalizedBy(runTool) }
                    RunAt.MANUAL -> Unit
                }
            }
        }
    }
}

class ToolArguments(
    @get:Input val moduleName: String,
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) val sourceRoots: ConfigurableFileCollection,
    @get:Classpath val analyzedClasspath: ConfigurableFileCollection,
    @get:Input @get:Optional val languageVersion: Provider<String>,
    @get:Input @get:Optional val apiVersion: Provider<String>,
    @get:Input @get:Optional val jvmTarget: Provider<String>,
    @get:OutputFile @get:Optional val output: Property<File>,
) : CommandLineArgumentProvider {
    override fun asArguments(): List<String> = buildList {
        add("--module-name"); add(moduleName)
        sourceRoots.forEach { add("--source-root"); add(it.absolutePath) }
        add("--classpath"); add(analyzedClasspath.joinToString(File.pathSeparator) { it.absolutePath })
        languageVersion.orNull?.let { add("--language-version"); add(it) }
        apiVersion.orNull?.let { add("--api-version"); add(it) }
        jvmTarget.orNull?.let { add("--jvm-target"); add(it) }
        output.orNull?.let { add("--output"); add(it.absolutePath) }
    }
}
