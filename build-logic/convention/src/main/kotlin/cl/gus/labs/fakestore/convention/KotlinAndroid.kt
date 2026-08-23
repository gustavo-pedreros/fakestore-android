package cl.gus.labs.fakestore.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    val javaVersion = JavaVersion.toVersion(libs.version("jvmTarget"))

    commonExtension.apply {
        compileSdk {
            version = release(libs.intVersion("compileSdk"))
        }
        defaultConfig.apply {
            minSdk = libs.intVersion("minSdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        compileOptions.apply {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }

    extensions.getByType<KotlinAndroidProjectExtension>()
        .jvmToolchain(libs.intVersion("jvmTarget"))
}