plugins {
    id("fakestore.jvm.library")
}

dependencies {
    api(platform(libs.junit.bom))
    api(libs.kotlinx.coroutines.test)
    api(libs.junit.jupiter.api)

    // Only for RobolectricDefaults; modules that run Robolectric bring it at runtime.
    compileOnly(libs.robolectric.annotations)
    compileOnly(libs.robolectric.pluginapi)
}
