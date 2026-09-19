package cl.gus.labs.fakestore.convention

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Per-module Kover setup.
 *
 * Every module creates a report variant under the same name, so the root project can merge them all
 * into a single report. Android modules contribute their `debug` build variant — `release` is the
 * minified and obfuscated one and is not worth measuring — and pure JVM modules contribute `jvm`.
 *
 * Neither is added as optional: a module whose variant is named something else must fail the build
 * rather than quietly report 0%.
 *
 * Must never be applied from inside an `afterEvaluate` block: Kover finalizes its variant locator on
 * the first one that drains its queue, and anything registering later fails with
 * `Attempt to queue after finalizing`.
 */
class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        val isAndroid = pluginManager.hasPlugin("com.android.library") ||
            pluginManager.hasPlugin("com.android.application")

        extensions.configure<KoverProjectExtension> {
            currentProject {
                createVariant(COVERAGE_VARIANT) {
                    add(if (isAndroid) "debug" else "jvm")
                }
            }

            reports {
                // The same list the merging module applies. Filters do not cross module boundaries in
                // either direction, so without this a module's own report counts the generated classes
                // and the DI wiring that Codecov never sees, and reads far below the real number.
                filters {
                    excludes {
                        excludeGeneratedAndWiring()
                    }
                }

                // The total variant merges debug *and* release, so it double counts Android classes,
                // and its verify task is wired into `check` by convention with `upToDateWhen { false }`.
                // Left alone it would add a forced, always-rerunning task to every `./gradlew build`.
                total {
                    verify {
                        onCheck.set(false)
                    }
                }
            }
        }
    }
}

internal const val COVERAGE_VARIANT = "coverage"
