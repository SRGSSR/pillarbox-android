plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.publishing)
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

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.media3.common)
    api(libs.androidx.media3.datasource)
    runtimeOnly(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    api(libs.okhttp)
}
