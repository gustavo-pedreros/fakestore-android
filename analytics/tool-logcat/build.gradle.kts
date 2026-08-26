// :analytics:tool-logcat
//
// The ONLY module allowed to see the Logcat SDK. Nothing depends on this except :analytics:bundle.
plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
}

android {
    namespace = "cl.gus.labs.fakestore.analytics.tool.logcat"
}

dependencies {
    implementation(project(":analytics:api"))
}
