/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

// https://developer.android.com/jetpack/androidx/explorer
object Version {
    const val core = "1.8.0"
    const val coroutines = "1.6.4"
    const val appCompat = "1.4.2"
    const val material = "1.6.1"
    const val media3 = "1.0.0"
    const val media = "1.6.0"
    const val glide = "4.14.2"
    const val gson = "2.9.1"
    const val retrofit = "2.9.0"
    const val moshi = "1.14.0"
    const val okhttp = "4.9.1"
    const val junit = "4.13.2"
    const val androidJunit = "1.1.3"
    const val espresso = "3.4.0"
    const val lifecycle = "2.5.1"
    const val fragment = "1.5.5"
    const val navigation = "2.5.3"
    const val activity = "1.6.1"
    const val composeCompiler = "1.3.2"
    const val composeUi = "1.3.2"
    const val composeMaterial = "1.3.1"
    const val detetk = "1.22.0"
    const val mockk = "1.13.4"
    const val tagCommanderCore = "5.3.2"
    const val tagCommanderServerSide = "5.4.0"
}

object Dependencies {

    object AndroidX {
        const val core = "androidx.core:core-ktx:${Version.core}"
        const val appCompat = "androidx.appcompat:appcompat:${Version.appCompat}"
        const val media = "androidx.media:media:${Version.media}"
        const val fragment = "androidx.fragment:fragment-ktx:${Version.fragment}"
        const val navigationUi = "androidx.navigation:navigation-ui-ktx:${Version.navigation}"
        const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Version.navigation}"
        const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.lifecycle}"
        const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${Version.lifecycle}"
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
        const val material = "androidx.compose.material:material:${Version.composeMaterial}"
        const val materialIconsExtended = "androidx.compose.material:material-icons-extended:${Version.composeMaterial}"
        const val ui = "androidx.compose.ui:ui:${Version.composeUi}"
        const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:${Version.composeUi}"
        const val uiTestJunit4 = "androidx.compose.ui:ui-test-junit4:${Version.composeUi}"
        const val uiTooling = "androidx.compose.ui:ui-tooling:${Version.composeUi}"
        const val uiTestManifest = "androidx.compose.ui:ui-test-manifest:${Version.composeUi}"
        const val activity = "androidx.activity:activity-compose:${Version.activity}"
        const val navigation = "androidx.navigation:navigation-compose:${Version.navigation}"
        const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-compose:${Version.lifecycle}"
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
        const val gson = "com.google.code.gson:gson:${Version.gson}"
        const val material = "com.google.android.material:material:${Version.material}"
    }

    object Test {
        const val junit = "junit:junit:${Version.junit}"
        const val androidJunit = "androidx.test.ext:junit:${Version.androidJunit}"
        const val espressoCore = "androidx.test.espresso:espresso-core:${Version.espresso}"
        const val mockk = "io.mockk:mockk:${Version.mockk}"
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
}
