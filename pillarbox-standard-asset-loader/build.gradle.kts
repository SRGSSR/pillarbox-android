plugins {
    alias(libs.plugins.pillarbox.android.library)
    // Currently we choose to not publish this module
    //alias(libs.plugins.pillarbox.android.library.publishing)
    alias(libs.plugins.pillarbox.android.library.tested.module)
    alias(libs.plugins.kotlin.serialization)
}

android {
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":pillarbox-player"))
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.media3.common)
    runtimeOnly(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.serialization.core)
    api(libs.okhttp)
}
