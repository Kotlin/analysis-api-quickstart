package org.example.tool

import com.intellij.util.PathUtil
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class SmokeTest {
    @TempDir
    lateinit var sourceRoot: Path

    @Test
    fun `dumps public api of a simple class`() {
        sourceRoot.resolve("test.kt").writeText(
            """
            class User(val name: String, val age: Int)

            private fun secretHelper() {}
            """.trimIndent()
        )

        val stdlib = Path(PathUtil.getJarPathForClass(Unit::class.java))
        val report = runTool(
            ToolInput(
                moduleName = "test-module",
                sourceRoots = listOf(sourceRoot),
                classpath = listOf(stdlib),
                languageVersion = "2.4",
            )
        )

        assertContains(report, "User")
        assertContains(report, "name")
        assertContains(report, "age")
        assertContains(report, "String")
        assertFalse("secretHelper" in report, "private declarations must not be in the report:\n$report")
    }
}
