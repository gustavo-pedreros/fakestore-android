package cl.gus.labs.fakestore.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("com.android.application") {
            pluginManager.apply("de.mannodermaus.android-junit5")
        }
        pluginManager.withPlugin("com.android.library") {
            pluginManager.apply("de.mannodermaus.android-junit5")
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            tasks.withType<Test>().configureEach { useJUnitPlatform() }
        }

        dependencies {
            add("testImplementation", platform(libs.library("junit-bom")))
            add("testImplementation", libs.bundle("test-unit"))
            add("testImplementation", libs.library("kotlinx-coroutines-test"))
            add("testRuntimeOnly", libs.library("junit-platform-launcher"))
        }
    }
}
