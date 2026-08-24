plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
}

android {
    namespace = "cl.gus.labs.fakestore.core.connectivity"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
