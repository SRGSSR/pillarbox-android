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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/4.1/com/android/build/api/dsl/LintOptions
        abortOnError = false
    }
    namespace = "ch.srgssr.pillarbox.player"
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:${Dependencies.ktxVersion}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Dependencies.coroutinesVersion}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Dependencies.coroutinesVersion}")
    // MediaSession MediaController use guava ListenableFuture
    api("org.jetbrains.kotlinx:kotlinx-coroutines-guava:${Dependencies.coroutinesVersion}")

    api("androidx.media3:media3-exoplayer:${Dependencies.media3Version}")
    api("androidx.media3:media3-session:${Dependencies.media3Version}")
    api("androidx.media:media:${Dependencies.mediaVersion}")
    api("androidx.media3:media3-ui:${Dependencies.media3Version}")
    implementation("androidx.media3:media3-exoplayer-dash:${Dependencies.media3Version}")
    implementation("androidx.media3:media3-exoplayer-hls:${Dependencies.media3Version}")
    implementation("com.github.bumptech.glide:glide:${Dependencies.glideVersion}")
    kapt("com.github.bumptech.glide:compiler:${Dependencies.glideVersion}")
    testImplementation("junit:junit:${Dependencies.testVersion}")
    androidTestImplementation("androidx.test.ext:junit:${Dependencies.androidTestVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Dependencies.espressoVersion}")
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
