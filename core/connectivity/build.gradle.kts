plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
}

android {
    namespace = "cl.gus.labs.fakestore.core.connectivity"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
}
