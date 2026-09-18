package org.example.tool

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.rendering.KaRenderer
import org.jetbrains.kotlin.analysis.api.rendering.renderToString
import org.jetbrains.kotlin.analysis.api.session.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.analysis.api.symbols.symbol
import org.jetbrains.kotlin.psi.KtFile

/**
 * The tool's logic — replace it with your own.
 *
 * This sample dumps the public API of the module: for every top-level declaration
 * it resolves the symbol and renders its signature.
 */
@OptIn(KaExperimentalApi::class)
fun runTool(input: ToolInput): String {
    val session = createAnalysisSession(input)
    val files = session.modulesWithFiles.values.single().filterIsInstance<KtFile>().sortedBy { it.name }

    return buildString {
        this.appendLine("Public API of module '${input.moduleName}'")
        for (file in files) {
            this.appendLine()
            this.appendLine("// ${file.name}")
            analyze(file) {
                for (declaration in file.declarations) {
                    val symbol = declaration.symbol
                    if (symbol.visibility != KaSymbolVisibility.PUBLIC) {
                        continue
                    }

                    val symbolText = KaRenderer.default.renderToString(symbol)
                    this.appendLine(symbolText)
                }
            }
        }
    }
}
