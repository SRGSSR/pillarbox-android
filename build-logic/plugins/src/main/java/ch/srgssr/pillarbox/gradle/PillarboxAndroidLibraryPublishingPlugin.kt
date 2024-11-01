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
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import java.net.URI

/**
 * Custom Gradle plugin to configure publication in an Android library module for Pillarbox.
 */
class PillarboxAndroidLibraryPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.gradle.maven-publish")
        pluginManager.apply("org.jetbrains.dokka")

        val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
            val dokkaHtmlTask = tasks.named("dokkaHtml")

            dependsOn(dokkaHtmlTask)
            from(dokkaHtmlTask.map { it.outputs })
            archiveClassifier.set("html-docs")
        }

        val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
            val dokkaJavadocTask = tasks.named("dokkaJavadoc")

            dependsOn(dokkaJavadocTask)
            from(dokkaJavadocTask.map { it.outputs })
            archiveClassifier.set("javadoc")
        }

        extensions.configure<LibraryExtension> {
            defaultConfig {
                group = "ch.srgssr.pillarbox"
                version = VersionConfig().versionName()
            }

            publishing {
                singleVariant("release") {
                    withSourcesJar()
                }
            }
        }

        extensions.configure<PublishingExtension> {
            publications {
                register<MavenPublication>("gpr") {
                    artifact(dokkaHtmlJar)
                    artifact(dokkaJavadocJar)

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

        extensions.configure<DokkaExtension> {
            dokkaSourceSets.getByName("main") {
                if (file("Module.md").exists()) {
                    includes.from("Module.md")
                } else {
                    includes.from("docs/README.md")
                }

                // This is currently broken in Dokka for Android modules. See: https://github.com/Kotlin/dokka/issues/2876
                sourceLink {
                    val version = VersionConfig().versionName(default = name)

                    localDirectory.set(projectDir.resolve("src"))
                    remoteUrl.set(URI("https://github.com/SRGSSR/pillarbox-android/tree/$version/${target.name}/src"))
                }
            }

            // Follow https://github.com/Kotlin/dokka/issues/3883 to see if it's necessary to duplicate this config
            pluginsConfiguration.getByName<DokkaHtmlPluginParameters>("html") {
                customStyleSheets.from(rootProject.projectDir.resolve("dokka/styles/pillarbox.css"))
                footerMessage.set("© SRG SSR")
                // TODO Enable this once we have some content there
                // homepageLink.set("https://android.pillarbox.ch/")
                templatesDir.set(rootProject.projectDir.resolve("dokka/templates"))
            }
        }
    }
}
