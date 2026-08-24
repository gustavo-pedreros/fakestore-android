plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
}

android {
    namespace = "cl.gus.labs.fakestore.favorites.data"
}

dependencies {
    implementation(project(":favorites:domain"))
    implementation(project(":core:database"))
}
