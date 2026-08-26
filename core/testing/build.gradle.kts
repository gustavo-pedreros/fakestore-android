plugins {
    id("fakestore.jvm.library")
}

dependencies {
    api(project(":analytics:api"))
    api(platform(libs.junit.bom))
    api(libs.kotlinx.coroutines.test)
    api(libs.junit.jupiter.api)
}
