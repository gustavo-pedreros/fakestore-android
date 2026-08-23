plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "cl.gus.labs.fakestore.core.network"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":shared:kernel"))
    api(project(":core:common"))

    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(platform(libs.retrofit.bom))
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.okhttp.mockwebserver)
}
