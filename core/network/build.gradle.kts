plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.junit5)
}

android {
    namespace = "cl.gus.labs.fakestore.core.network"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test.unit)
    testRuntimeOnly(libs.junit.platform.launcher)
}
