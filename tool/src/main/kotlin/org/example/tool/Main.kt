package org.example.tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.system.exitProcess

data class ToolInput(
    val moduleName: String,
    val sourceRoots: List<Path>,
    val classpath: List<Path>,
    val languageVersion: String? = null,
    val apiVersion: String? = null,
    val jvmTarget: String? = null,
)

class ToolCommand : CliktCommand(name = "tool") {
    override val treatUnknownOptionsAsArgs: Boolean = true

    private val moduleName by option("--module-name").required()

    private val sourceRoots by option("--source-root", help = "Kotlin source root, repeatable")
        .path()
        .multiple(required = true)

    private val classpath by option("--classpath", help = "Compile classpath, '${File.pathSeparator}'-separated")
        .convert { raw -> raw.split(File.pathSeparator).filter { it.isNotBlank() }.map { Path(it) } }
        .default(emptyList())

    private val languageVersion by option("--language-version")

    private val apiVersion by option("--api-version")

    private val jvmTarget by option("--jvm-target")

    private val output by option("--output").file(mustExist = false)

    override fun run() {
        val input = ToolInput(moduleName, sourceRoots, classpath, languageVersion, apiVersion, jvmTarget)
        val result = runTool(input)

        val output = output
        if (output != null) {
            output.writeText(result)
        } else {
            println(result)
        }
    }
}

fun main(args: Array<String>) {
    try {
        ToolCommand().main(args)
        // KT-73127
        exitProcess(0)
    } catch (e: Throwable) {
        e.printStackTrace()
        exitProcess(1)
    }
}
