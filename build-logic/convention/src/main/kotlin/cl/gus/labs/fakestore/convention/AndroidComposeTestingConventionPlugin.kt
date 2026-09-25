package cl.gus.labs.fakestore.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/**
 * Compose UI and screenshot tests on the JVM: Robolectric renders the UI with native graphics and
 * Roborazzi records and compares the images. It all runs in `testDebugUnitTest`, so it needs no
 * device and Kover measures it.
 *
 * Robolectric, Compose's test rules and Roborazzi are JUnit 4, so the vintage engine goes next to the
 * JUnit 5 setup of `fakestore.testing`, which this plugin applies.
 *
 * Roborazzi captures nothing unless a mode is on (`-Proborazzi.test.verify=true`, or one of its
 * `record`/`verify`/`compare` tasks): a plain `test` run passes over every screenshot.
 */
class AndroidComposeTestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(TestingConventionPlugin::class.java)
        pluginManager.apply("io.github.takahirom.roborazzi")

        pluginManager.withPlugin("com.android.application") {
            extensions.getByType<ApplicationExtension>().includeAndroidResourcesInUnitTests()
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.getByType<LibraryExtension>().includeAndroidResourcesInUnitTests()
        }

        tasks.withType<Test>().configureEach {
            systemProperty("robolectric.graphicsMode", "NATIVE")
            systemProperty("robolectric.pixelCopyRenderMode", "hardware")
            // Previews format dates in the default zone: same image on every machine.
            systemProperty("user.timezone", "UTC")
        }

        // Baselines are committed next to the tests. Diffs of a failed comparison stay in build/, where
        // CI can upload them and git never sees them.
        extensions.configure<RoborazziExtension> {
            outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
            compare {
                outputDir.set(layout.buildDirectory.dir("outputs/roborazzi"))
            }
        }

        dependencies {
            add("testImplementation", libs.library("junit4"))
            add("testImplementation", libs.library("robolectric"))
            add("testImplementation", platform(libs.library("androidx-compose-bom")))
            add("testImplementation", libs.library("androidx-compose-ui-test-junit4"))
            add("testImplementation", libs.bundle("test-screenshot"))
            add("testRuntimeOnly", libs.library("junit-vintage-engine"))
            add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
        }
    }
}

// Fonts, drawables and plurals load through resources, which unit tests do not package by default.
private fun CommonExtension.includeAndroidResourcesInUnitTests() {
    testOptions.unitTests.isIncludeAndroidResources = true
}