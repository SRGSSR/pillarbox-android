import org.gradle.api.tasks.testing.logging.TestExceptionFormat

/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.kover)
    `maven-publish`
}

android {
    namespace = "ch.srgssr.pillarbox.core.business"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        version = VersionConfig.versionName()
        group = VersionConfig.GROUP

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }
    kotlinOptions {
        jvmTarget = AppConfig.javaVersion.majorVersion
    }
    buildFeatures {
        buildConfig = true
        resValues = false
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging.exceptionFormat = TestExceptionFormat.FULL
}

dependencies {
    api(project(":pillarbox-analytics"))
    api(project(":pillarbox-player"))

    implementation(libs.androidx.annotation)
    implementation(libs.comscore)
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.media3.common)
    api(libs.androidx.media3.datasource)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.guava)
    runtimeOnly(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.http)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.utils)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.tagcommander.core)

    testImplementation(libs.androidx.media3.test.utils)
    testImplementation(libs.androidx.media3.test.utils.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.monitor)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.dsl)
    testRuntimeOnly(libs.robolectric)
    testRuntimeOnly(libs.robolectric.shadows.framework)

    androidTestImplementation(project(":pillarbox-player-testutils"))

    androidTestImplementation(libs.androidx.test.monitor)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.junit)
    androidTestRuntimeOnly(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

koverReport {
    androidReports("debug") {
        xml {
            title.set(project.path)
        }
    }
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
