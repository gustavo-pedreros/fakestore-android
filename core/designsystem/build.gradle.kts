plugins {
    id("fakestore.android.library")
    id("fakestore.android.compose")
    id("fakestore.testing")
    id("fakestore.android.compose.testing")
}

android {
    namespace = "cl.gus.labs.fakestore.core.designsystem"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose.library)
    api(libs.androidx.compose.ui.text)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.coil.test)

    debugImplementation(libs.bundles.compose.debug)
}
