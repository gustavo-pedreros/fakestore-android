plugins {
    id("fakestore.jvm.library")
}

dependencies {
    api(platform(libs.junit.bom))
    api(libs.kotlinx.coroutines.test)
    api(libs.junit.jupiter.api)
}
