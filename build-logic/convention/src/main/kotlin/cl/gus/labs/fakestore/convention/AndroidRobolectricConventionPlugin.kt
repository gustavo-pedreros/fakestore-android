package cl.gus.labs.fakestore.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Android framework tests on the JVM with Robolectric.
 *
 * Robolectric is JUnit 4, so the vintage engine goes next to the JUnit 5 setup of `fakestore.testing`,
 * which this plugin applies; without it those tests would not run at all.
 *
 * Every test runs at `robolectricSdk` from the version catalog, which `RobolectricDefaults` in
 * `:core:testing` reads from a system property. Otherwise a module without its own `sdk` would fall back
 * to Robolectric's oldest SDK, below `minSdk`.
 */
class AndroidRobolectricConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(TestingConventionPlugin::class.java)

        val sdk = libs.intVersion("robolectricSdk")
        val minSdk = libs.intVersion("minSdk")
        check(sdk >= minSdk) { "robolectricSdk $sdk is below minSdk $minSdk" }

        tasks.withType<Test>().configureEach {
            systemProperty("fakestore.robolectric.sdk", sdk)
        }

        dependencies {
            add("testImplementation", libs.library("junit4"))
            add("testImplementation", libs.library("robolectric"))
            add("testRuntimeOnly", libs.library("junit-vintage-engine"))
        }
    }
}
