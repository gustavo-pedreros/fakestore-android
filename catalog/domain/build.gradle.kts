plugins {
    id("fakestore.jvm.library")
    id("fakestore.testing")
}

dependencies {
    api(project(":shared:kernel"))
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
}