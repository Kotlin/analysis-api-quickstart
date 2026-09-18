package org.example.toolplugin

import org.gradle.api.provider.Property
import java.io.File

enum class RunAt {
    /** The tool runs only when the `runTool` task is invoked explicitly. */
    MANUAL,

    /** `compileKotlin` waits for the tool (e.g. for code generation). */
    BEFORE_COMPILATION,

    /** The tool runs right after `compileKotlin` (e.g. for checks). */
    AFTER_COMPILATION,
}

abstract class ToolExtension {
    abstract val runAt: Property<RunAt>
    abstract val output: Property<File>
}
