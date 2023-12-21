/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "ch.srgssr.pillarbox.player"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        version = VersionConfig.versionName()
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
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    // Mockk includes some licenses information, which may conflict with other license files. This block merges all licenses together.
    // Mockk excludes all licenses instead:
    // https://github.com/mockk/mockk/blob/f879502a044c83c2a5fd52992f20903209eb34f3/modules/mockk-android/build.gradle.kts#L14-L19
    packaging {
        resources {
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core)
    implementation(libs.androidx.media)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.dash)
    api(libs.androidx.media3.datasource)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.hls)
    api(libs.androidx.media3.session)
    api(libs.androidx.media3.ui)
    api(libs.guava)
    runtimeOnly(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.core)

    testImplementation(project(":pillarbox-player-testutils"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.dsl)

    androidTestImplementation(project(":pillarbox-player-testutils"))

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.monitor)
    androidTestRuntimeOnly(libs.androidx.test.runner)
    androidTestImplementation(libs.junit)
    androidTestRuntimeOnly(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.dsl)
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
