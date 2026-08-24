plugins {
    id("fakestore.jvm.library")
    id("fakestore.testing")
}

dependencies {
    api(project(":shared:kernel"))
    api(libs.kotlinx.coroutines.core)
}
