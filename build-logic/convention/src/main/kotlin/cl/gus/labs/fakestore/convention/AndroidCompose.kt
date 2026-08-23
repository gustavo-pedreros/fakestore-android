package cl.gus.labs.fakestore.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    commonExtension.buildFeatures.compose = true
    configureComposeCompilerReports()
}

//Compose compiler metrics and reports, off unless asked for
private fun Project.configureComposeCompilerReports() {
    if (!providers.gradleProperty("composeCompilerReports").isPresent) return
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        val destination = layout.buildDirectory.dir("compose-compiler")
        reportsDestination.set(destination)
        metricsDestination.set(destination)
    }
}
