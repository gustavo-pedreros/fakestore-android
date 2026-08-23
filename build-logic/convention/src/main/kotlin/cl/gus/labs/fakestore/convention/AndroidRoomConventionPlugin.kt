package cl.gus.labs.fakestore.convention

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<KspExtension> {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        dependencies {
            add("implementation", libs.library("room-runtime"))
            add("implementation", libs.library("room-ktx"))
            add("ksp", libs.library("room-compiler"))
        }
    }
}
