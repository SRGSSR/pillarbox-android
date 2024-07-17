/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle

import com.android.build.api.dsl.LibraryExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Custom Gradle plugin to configure testing in an Android library module for Pillarbox.
 */
class PillarboxAndroidLibraryTestedModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        extensions.configure<LibraryExtension> {
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            @Suppress("UnstableApiUsage")
            testOptions {
                unitTests {
                    isIncludeAndroidResources = true
                }
            }
        }

        extensions.configure<KoverProjectExtension> {
            reports {
                variant("debug") {
                    xml {
                        title = project.path
                    }
                }
            }
        }

        tasks.withType<Test>().configureEach {
            testLogging.exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
