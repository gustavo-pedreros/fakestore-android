plugins {
    id("fakestore.android.application")
    id("fakestore.android.compose")
    id("fakestore.android.hilt")
    id("fakestore.testing")
}

android {
    namespace = "cl.gus.labs.fakestore"

    defaultConfig {
        applicationId = "cl.gus.labs.fakestore"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":catalog:ui"))
    implementation(project(":catalog:data")) // solo para que Hilt agregue sus @Module
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    androidTestImplementation(platform(libs.junit.bom))
    androidTestImplementation(libs.bundles.test.instrumented)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
