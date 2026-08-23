plugins {
    id("fakestore.android.library")
    id("fakestore.android.compose")
    id("fakestore.testing")
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

    debugImplementation(libs.bundles.compose.debug)

    androidTestImplementation(platform(libs.junit.bom))
    androidTestImplementation(libs.bundles.test.instrumented)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
