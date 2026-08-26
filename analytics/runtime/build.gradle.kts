// :analytics:runtime
//
// Depends on :api and Hilt. It must NEVER depend on an :analytics:tool-* module — that is what
// the multibinding exists to avoid. Vendors belong in :analytics:bundle.
plugins {
    id("fakestore.android.library")
    id("fakestore.android.hilt")
    id("fakestore.testing")
}

android {
    namespace = "cl.gus.labs.fakestore.analytics.runtime"
}

dependencies {
    api(project(":analytics:api"))
}
