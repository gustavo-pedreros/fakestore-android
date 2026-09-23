package cl.gus.labs.fakestore.convention

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Root-only Kover setup: the merging module.
 *
 * Merges the `coverage` variant of every production module into one report, and owns the verification
 * rule — Kover only honours that in the merging module, so the same block in a submodule would be
 * ignored without warning. Exclusions are shared instead, see [excludeGeneratedAndWiring].
 *
 * The root has no Kotlin or Android plugin, so it creates the variant empty and contributes only the
 * variants that arrive through the `kover` dependencies below.
 */
class KoverRootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        val measured = subprojects.filter { it.buildFile.exists() && it.path !in MODULES_WITHOUT_COVERAGE }
        dependencies {
            measured.forEach { add("kover", project(it.path)) }
        }

        extensions.configure<KoverProjectExtension> {
            currentProject {
                createVariant(COVERAGE_VARIANT) { }
            }

            reports {
                filters {
                    excludes {
                        excludeGeneratedAndWiring()
                    }
                }

                variant(COVERAGE_VARIANT) {
                    verify {
                        rule {
                            minBound(PROJECT_COVERAGE_TARGET)
                        }
                    }
                }
            }
        }
    }
}

private const val PROJECT_COVERAGE_TARGET = 60
private val MODULES_WITHOUT_COVERAGE = setOf(":core:testing")
