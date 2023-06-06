/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    `maven-publish`
}

android {
    namespace = "ch.srgssr.pillarbox.player"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        version = VersionConfig.getLibraryVersionNameFromProject(project)
        group = VersionConfig.GROUP

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/4.1/com/android/build/api/dsl/LintOptions
        abortOnError = false
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(Dependencies.AndroidX.core)
    api(Dependencies.Coroutines.android)
    api(Dependencies.Coroutines.core)
    // MediaSession MediaController use guava ListenableFuture
    api(Dependencies.Coroutines.guava)
    // Exoplayer/Media3 need guava-android version and not jre! https://github.com/google/ExoPlayer/issues/9704
    implementation(Dependencies.Google.guavaAndroid)

    api(Dependencies.AndroidX.media)
    api(Dependencies.Media3.exoplayer)
    implementation(Dependencies.Media3.dash)
    implementation(Dependencies.Media3.hls)
    api(Dependencies.Media3.session)
    api(Dependencies.Media3.ui)

    implementation(Dependencies.Glide.glide)
    kapt(Dependencies.Glide.glideCompiler)

    testImplementation(Dependencies.Test.junit)
    testImplementation(Dependencies.Coroutines.test)
    testImplementation(Dependencies.Test.mockk)
    testImplementation(project(mapOf("path" to ":pillarbox-player-testutils")))

    androidTestImplementation(Dependencies.Test.androidJunit)
    androidTestImplementation(Dependencies.Test.espressoCore)
    androidTestImplementation(Dependencies.Test.mockkAndroid)
    androidTestImplementation(project(mapOf("path" to ":pillarbox-player-testutils")))
}

publishing {
    publications {
        register<MavenPublication>("gpr") {
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SRGSSR/pillarbox-android")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password =
                    project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
