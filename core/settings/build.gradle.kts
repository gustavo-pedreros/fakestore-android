plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
}

android {
    namespace = "cl.gus.labs.fakestore.core.settings"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.preferences)
}
