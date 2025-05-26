/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle

import ch.srgssr.pillarbox.gradle.internal.AppConfig
import ch.srgssr.pillarbox.gradle.internal.VersionConfig
import ch.srgssr.pillarbox.gradle.internal.configureAndroidLintModule
import ch.srgssr.pillarbox.gradle.internal.configureAndroidModule
import ch.srgssr.pillarbox.gradle.internal.configureKotlinModule
import ch.srgssr.pillarbox.gradle.internal.libs
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra

/**
 * Custom Gradle plugin to configure an Android library module for Pillarbox.
 */
class PillarboxAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("com.autonomousapps.dependency-analysis")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            configureAndroidLintModule(this)
            configureAndroidModule(this)
            configureKotlinModule()

            buildFeatures {
                compose = true
            }

            defaultConfig {
                applicationId = namespace
                androidResources.localeFilters += "en"
                targetSdk = AppConfig.targetSdk
                versionCode = VersionConfig().versionCode()
                versionName = VersionConfig().versionName()
                vectorDrawables.useSupportLibrary = true
            }

            signingConfigs {
                register("release") {
                    val password = System.getenv("DEMO_KEY_PASSWORD") ?: extra.properties["pillarbox.keystore.password"] as String?

                    storeFile = file("./demo.keystore")
                    storePassword = password
                    keyAlias = "demo"
                    keyPassword = password
                }
            }

            buildTypes {
                debug {
                    applicationIdSuffix = ".debug"
                    versionNameSuffix = "-debug"
                }

                release {
                    signingConfig = signingConfigs.getByName("release")
                    isMinifyEnabled = false
                    isDebuggable = true

                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }
        }

        dependencies {
            add("coreLibraryDesugaring", libs.findLibrary("desugar_jdk_libs").get())
        }
    }
}
