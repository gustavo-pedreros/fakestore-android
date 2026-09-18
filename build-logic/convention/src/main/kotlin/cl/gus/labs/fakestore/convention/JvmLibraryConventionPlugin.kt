package cl.gus.labs.fakestore.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply(KoverConventionPlugin::class.java)
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(libs.intVersion("jvmTarget"))
        }
    }
}