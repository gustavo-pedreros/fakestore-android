package cl.gus.labs.fakestore.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(name: String): String =
    findVersion(name)
        .orElseThrow { IllegalStateException("No version '$name' in the version catalog") }
        .requiredVersion

internal fun VersionCatalog.intVersion(name: String): Int = version(name).toInt()

internal fun VersionCatalog.library(name: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(name)
        .orElseThrow { IllegalStateException("No library '$name' in the version catalog") }