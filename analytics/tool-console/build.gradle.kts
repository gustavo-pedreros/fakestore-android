// :analytics:tool-console
//
// The ONLY module allowed to see the Console SDK. Nothing depends on this except :analytics:bundle.
plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
}

android {
    namespace = "cl.gus.labs.fakestore.analytics.tool.console"
}

dependencies {
    implementation(project(":analytics:api"))
}
