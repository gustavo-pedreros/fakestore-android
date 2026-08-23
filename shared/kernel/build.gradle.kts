plugins {
    id("fakestore.jvm.library")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test.unit)
    testRuntimeOnly(libs.junit.platform.launcher)
}