plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.publishing)
    alias(libs.plugins.pillarbox.android.library.tested.module)
}

dependencies {
    implementation(project(":pillarbox-core-business"))
    implementation(project(":pillarbox-core-business-cast"))
    implementation(project(":pillarbox-player"))

    api(libs.play.services.cast.tv)
    implementation(libs.play.services.cast)
}
