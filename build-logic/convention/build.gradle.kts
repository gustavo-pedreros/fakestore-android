plugins {
    `kotlin-dsl`
}

group = "cl.gus.labs.fakestore.buildlogic"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvmTarget.get().toInt())
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.hilt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("jvmLibrary") {
            id = "fakestore.jvm.library"
            implementationClass = "cl.gus.labs.fakestore.convention.JvmLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "fakestore.android.application"
            implementationClass = "cl.gus.labs.fakestore.convention.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "fakestore.android.library"
            implementationClass = "cl.gus.labs.fakestore.convention.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "fakestore.android.compose"
            implementationClass = "cl.gus.labs.fakestore.convention.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "fakestore.android.hilt"
            implementationClass = "cl.gus.labs.fakestore.convention.AndroidHiltConventionPlugin"
        }
    }
}
