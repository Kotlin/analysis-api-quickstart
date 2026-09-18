package org.example.tool

import org.jetbrains.kotlin.analysis.api.standalone.StandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSdkModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import kotlin.io.path.Path
import kotlin.io.path.exists

/** Builds a standalone Analysis API session with a single source module described by [input]. */
fun createAnalysisSession(input: ToolInput): StandaloneAnalysisAPISession =
    buildStandaloneAnalysisAPISession {
        buildKtModuleProvider {
            val jvmTarget = input.jvmTarget?.let { target ->
                JvmTarget.fromString(target) ?: error("Unknown JVM target: $target")
            } ?: JvmTarget.DEFAULT
            val targetPlatform = JvmPlatforms.jvmPlatformByTargetVersion(jvmTarget)
            platform = targetPlatform

            val sdk = buildKtSdkModule {
                libraryName = "JDK"
                platform = targetPlatform
                addBinaryRootsFromJdkHome(Path(System.getProperty("java.home")), isJre = false)
            }

            val libraries = buildKtLibraryModule {
                libraryName = "classpath"
                platform = targetPlatform
                addBinaryRoots(input.classpath.filter { it.exists() })
            }

            val sources = buildKtSourceModule {
                moduleName = input.moduleName
                platform = targetPlatform
                languageSettings(input)?.let { languageVersionSettings = it }
                addSourceRoots(input.sourceRoots.filter { it.exists() })
                addRegularDependency(sdk)
                addRegularDependency(libraries)
            }

            addModule(sdk)
            addModule(libraries)
            addModule(sources)
        }
    }

private fun languageSettings(input: ToolInput): LanguageVersionSettings? {
    if (input.languageVersion == null && input.apiVersion == null) return null
    val language = input.languageVersion
        ?.let { LanguageVersion.fromVersionString(it) ?: error("Unknown language version: $it") }
        ?: LanguageVersion.LATEST_STABLE
    val api = input.apiVersion
        ?.let { ApiVersion.parse(it) ?: error("Unknown API version: $it") }
        ?: ApiVersion.createByLanguageVersion(language)
    return LanguageVersionSettingsImpl(language, api)
}
