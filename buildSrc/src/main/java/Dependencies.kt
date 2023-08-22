/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

// https://developer.android.com/jetpack/androidx/explorer
// https://developer.android.com/jetpack/androidx/releases/compose-kotlin
object Version {
    const val core = "1.10.1"
    const val coroutines = "1.6.4"
    const val media3 = "1.1.0"
    const val guava = "31.1-android"
    const val media = "1.6.0"
    const val glide = "4.14.2"
    const val retrofit = "2.9.0"
    const val moshi = "1.15.0"
    const val okhttp = "4.9.1"
    const val junit = "4.13.2"
    const val androidJunit = "1.1.5"
    const val espresso = "3.5.1"
    const val fragment = "1.5.7"
    const val navigation = "2.7.0"
    const val activity = "1.7.2"
    const val composeCompiler = "1.5.1"
    const val composeUi = "1.5.0"
    const val composeMaterial = "1.5.0"
    const val composeBom = "2023.08.00"
    const val detetk = "1.22.0"

    /*
     * Downgrade mockk to 1.12.5 because of duplicate files with androidTest
     * https://stackoverflow.com/questions/75150167/instrumented-tests-will-not-run-6-files-found-with-path-meta-inf-license-md
     */
    const val mockk = "1.12.5"
    const val tagCommanderCore = "5.3.2"
    const val tagCommanderServerSide = "5.4.2"
    const val comscoreVersion = "6.9.3"
}

object Dependencies {

    object AndroidX {
        const val core = "androidx.core:core-ktx:${Version.core}"
        const val media = "androidx.media:media:${Version.media}"
    }

    object Coroutines {
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutines}"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutines}"
        const val guava = "org.jetbrains.kotlinx:kotlinx-coroutines-guava:${Version.coroutines}"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Version.coroutines}"
    }

    object Media3 {
        const val exoplayer = "androidx.media3:media3-exoplayer:${Version.media3}"
        const val session = "androidx.media3:media3-session:${Version.media3}"
        const val ui = "androidx.media3:media3-ui:${Version.media3}"
        const val dash = "androidx.media3:media3-exoplayer-dash:${Version.media3}"
        const val hls = "androidx.media3:media3-exoplayer-hls:${Version.media3}"
    }

    object Compose {
        // https://developer.android.com/jetpack/androidx/releases/compose
        const val bom = "androidx.compose:compose-bom:${Version.composeBom}"
        const val material = "androidx.compose.material3:material3"
        const val materialIconsExtended = "androidx.compose.material:material-icons-extended"
        const val ui = "androidx.compose.ui:ui"
        const val foundation = "androidx.compose.foundation:foundation"
        const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
        const val uiTestJunit4 = "androidx.compose.ui:ui-test-junit4"
        const val uiTooling = "androidx.compose.ui:ui-tooling"
        const val uiTestManifest = "androidx.compose.ui:ui-test-manifest"
        const val activity = "androidx.activity:activity-compose:${Version.activity}"
        const val navigation = "androidx.navigation:navigation-compose:${Version.navigation}"
    }

    object Glide {
        const val glide = ("com.github.bumptech.glide:glide:${Version.glide}")
        const val glideCompiler = "com.github.bumptech.glide:compiler:${Version.glide}"
    }

    object Square {
        const val retrofit = "com.squareup.retrofit2:retrofit:${Version.retrofit}"
        const val converterMoshi = "com.squareup.retrofit2:converter-moshi:${Version.retrofit}"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Version.okhttp}"
        const val moshi = "com.squareup.moshi:moshi:${Version.moshi}"
        const val moshiKotlinCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Version.moshi}"
    }


    object Google {
        const val guavaAndroid = "com.google.guava:guava:${Version.guava}"
    }

    object Test {
        const val junit = "junit:junit:${Version.junit}"
        const val androidJunit = "androidx.test.ext:junit-ktx:${Version.androidJunit}"
        const val espressoCore = "androidx.test.espresso:espresso-core:${Version.espresso}"
        const val mockk = "io.mockk:mockk:${Version.mockk}"
        const val mockkAndroid = "io.mockk:mockk-android:${Version.mockk}"
    }

    object Detekt {
        const val detektCli = "io.gitlab.arturbosch.detekt:detekt-cli:${Version.detetk}"
        const val detektFormatting = "io.gitlab.arturbosch.detekt:detekt-formatting:${Version.detetk}"
    }

    /**
     * https://github.com/CommandersAct/AndroidV5
     */
    object CommandersAct {
        const val tagcommanderCore = "com.tagcommander.lib:core:${Version.tagCommanderCore}"
        const val tagcommanderServerSide = "com.tagcommander.lib:ServerSide:${Version.tagCommanderServerSide}"
    }

    object Comscore {
        const val analytis = "com.comscore:android-analytics:${Version.comscoreVersion}"
    }
}
