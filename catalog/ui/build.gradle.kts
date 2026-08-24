plugins {
    id("fakestore.android.library")
    id("fakestore.android.compose")
    id("fakestore.android.hilt")
    id("fakestore.testing")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "cl.gus.labs.fakestore.catalog.ui"
}

dependencies {
    implementation(project(":catalog:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:connectivity"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)

    debugImplementation(libs.bundles.compose.debug)

    testImplementation(libs.turbine)
}
