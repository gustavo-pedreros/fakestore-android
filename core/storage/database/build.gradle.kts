plugins {
    id("fakestore.android.library")
    id("fakestore.android.room")
    id("fakestore.android.hilt")
    id("fakestore.testing")
    id("fakestore.android.robolectric")
}

android {
    namespace = "cl.gus.labs.fakestore.core.storage.database"
}
