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
    compileOnly(libs.kover.gradle.plugin)
    compileOnly(libs.roborazzi.gradle.plugin)
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
        register("androidComposeTesting") {
            id = "fakestore.android.compose.testing"
            implementationClass = "cl.gus.labs.fakestore.convention.AndroidComposeTestingConventionPlugin"
        }
        register("androidHilt") {
            id = "fakestore.android.hilt"
            implementationClass = "cl.gus.labs.fakestore.convention.AndroidHiltConventionPlugin"
        }
        register("testing") {
            id = "fakestore.testing"
            implementationClass = "cl.gus.labs.fakestore.convention.TestingConventionPlugin"
        }
        register("androidRoom") {
            id = "fakestore.android.room"
            implementationClass = "cl.gus.labs.fakestore.convention.AndroidRoomConventionPlugin"
        }
        register("kover") {
            id = "fakestore.kover"
            implementationClass = "cl.gus.labs.fakestore.convention.KoverConventionPlugin"
        }
        register("koverRoot") {
            id = "fakestore.kover.root"
            implementationClass = "cl.gus.labs.fakestore.convention.KoverRootConventionPlugin"
        }
        register("uiGallery") {
            id = "fakestore.ui.gallery"
            implementationClass = "cl.gus.labs.fakestore.convention.UiGalleryConventionPlugin"
        }
    }
}
