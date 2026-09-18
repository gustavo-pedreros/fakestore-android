package cl.gus.labs.fakestore.convention

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Root-only Kover setup: the merging module.
 *
 * Merges the `coverage` variant of every production module into one report, and owns every report
 * filter and verification rule — Kover only honours those in the merging module, so the same blocks
 * in a submodule would be ignored without warning.
 *
 * The root has no Kotlin or Android plugin, so it creates the variant empty and contributes only the
 * variants that arrive through the `kover` dependencies below.
 */
class KoverRootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        // `subprojects` also returns the container projects that `include(":catalog:data")` creates
        // implicitly — `:catalog`, `:core`, `:favorites`, `:shared`. They have no build script, so no
        // convention plugin, no Kover and no `coverage` variant to merge.
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
                        // Dependency injection wiring: cl.gus.labs.fakestore.di plus every
                        // cl.gus.labs.fakestore.<feature>.<layer>.di, subpackages included.
                        packages("cl.gus.labs.fakestore*.di")

                        // The one package that holds nothing but Compose previews.
                        packages("cl.gus.labs.fakestore.core.designsystem.preview")

                        // The rest of the previews are private functions at the bottom of production
                        // files, so only an annotation filter reaches them. @PreviewLightDark and
                        // friends are separate annotation classes and Kover does not resolve
                        // meta-annotations, hence the wildcard.
                        annotatedBy("androidx.compose.ui.tooling.preview.Preview*")

                        // Finishes the job of the annotation filter above. Excluding a declaration
                        // does not exclude its nested or anonymous classes, and the Compose compiler
                        // hoists the constant lambdas of a preview body into `ComposableSingletons$X`,
                        // which carries no annotation for `annotatedBy` to match. Measured: 21 such
                        // classes, 372 lines, none of them covered, and 20 of the 21 sit in a file
                        // that has previews — so nothing of value is lost today.
                        classes("*ComposableSingletons*")

                        // *Activity, *Fragment, *.BuildConfig, *.databinding.*
                        androidGeneratedClasses()

                        // KSP output: Hilt/Dagger factories and Room implementations. Patterns match
                        // the fully qualified name, so each needs a leading `*`. Each also needs a
                        // *trailing* `*`: without it the pattern matched nothing here, and it is
                        // needed anyway to catch the nested classes KSP emits alongside the outer one
                        // (`FavoriteDao_Impl$1`, `ProductDao_Impl$Companion`, ...).
                        classes(
                            "*_Factory*",
                            "*_Impl*",
                            "*Hilt_*",
                            "*HiltWrapper_*",
                            "*_HiltModules*",
                            "*_MembersInjector*",
                            "*_GeneratedInjector*",
                            "*Module_*Factory*",
                            "dagger.hilt.internal.*",
                            "hilt_aggregated_deps.*",
                        )
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

// Test infrastructure that lives in src/main (MainDispatcherExtension, FixedClock). It sits on the
// production classpath but it is not production code, and it has no tests of its own.
private val MODULES_WITHOUT_COVERAGE = setOf(":core:testing")

// Mirrors the Codecov `project` target so `./gradlew koverVerifyCoverage` is a local dry run of the
// same gate. Kover bounds are Int, so the 0.2% tolerance lives only in codecov.yml.
private const val PROJECT_COVERAGE_TARGET = 60
