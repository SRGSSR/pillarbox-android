/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle

import ch.srgssr.pillarbox.gradle.internal.VersionConfig
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

/**
 * Custom Gradle plugin to configure publication in an Android library module for Pillarbox.
 */
class PillarboxAndroidLibraryPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.gradle.maven-publish")

        extensions.configure<LibraryExtension> {
            defaultConfig {
                group = "ch.srgssr.pillarbox"
                version = VersionConfig.versionName()
            }

            publishing {
                singleVariant("release") {
                    withSourcesJar()
                    withJavadocJar()
                }
            }
        }

        extensions.configure<PublishingExtension> {
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
                        username = providers.gradleProperty("gpr.user")
                            .orElse(providers.environmentVariable("USERNAME"))
                            .get()
                        password = providers.gradleProperty("gpr.key")
                            .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                            .get()
                    }
                }
            }
        }
    }
}
