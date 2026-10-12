plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
}

android {
    namespace = "cl.gus.labs.fakestore.core.storage.keyvalue"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(project(":core:common"))
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.turbine)
}
