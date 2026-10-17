plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "cl.gus.labs.fakestore.catalog.data"
}

dependencies {
    implementation(project(":catalog:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:storage:database"))

    implementation(platform(libs.retrofit.bom))
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(platform(libs.okhttp.bom))
    testImplementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.turbine)
}
