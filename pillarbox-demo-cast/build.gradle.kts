plugins {
    alias(libs.plugins.pillarbox.android.application)
}


dependencies {
    // implementation(project(":pillarbox-analytics"))
    // implementation(project(":pillarbox-core-business"))
    implementation(project(":pillarbox-demo-shared"))
    // implementation(project(":pillarbox-player"))
    // implementation(project(":pillarbox-ui"))
    implementation(project(":pillarbox-cast"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.play.services.cast.framework)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
