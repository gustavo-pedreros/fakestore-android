// :analytics:bundle
//
// This module has no source. Its dependency list IS its content: the single place that answers
// "which analytics vendors does this product ship?".
//
// Adding a vendor: create :analytics:tool-<name>, add one line below.
// Removing a vendor: delete one line below.
// Neither touches :runtime, :api, the app, or any feature module.
plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
}

android {
    namespace = "cl.gus.labs.fakestore.analytics.bundle"
}

dependencies {
    api(project(":analytics:runtime"))

    implementation(project(":analytics:tool-logcat"))
    implementation(project(":analytics:tool-console"))
}
