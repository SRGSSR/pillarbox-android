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
import org.gradle.kotlin.dsl.register
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask
import org.jetbrains.kotlin.gradle.utils.named
import java.net.URI

/**
 * Custom Gradle plugin to configure publication in an Android library module for Pillarbox.
 */
class PillarboxAndroidLibraryPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.gradle.maven-publish")
        pluginManager.apply("org.jetbrains.dokka")
        pluginManager.apply("org.jetbrains.dokka-javadoc")

        val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
            val dokkaHtmlTask = tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml")

            dependsOn(dokkaHtmlTask)
            from(dokkaHtmlTask.flatMap { it.outputDirectory })
            archiveClassifier.set("html-docs")
        }

        val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
            val dokkaJavadocTask = tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationJavadoc")

            dependsOn(dokkaJavadocTask)
            from(dokkaJavadocTask.flatMap { it.outputDirectory })
            archiveClassifier.set("javadoc")
        }

        rootProject.dependencies.add("dokka", project(path))

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

                    pom {
                        name.set(project.name)
                        url.set("https://github.com/SRGSSR/pillarbox-android/")
                        inceptionYear.set("2022")

                        licenses {
                            license {
                                name.set("The MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }

                        organization {
                            name.set("SRG SSR")
                            url.set("https://www.srgssr.ch/")
                        }

                        scm {
                            connection.set("scm:git:git://github.com/SRGSSR/pillarbox-android.git")
                            developerConnection.set("scm:git:git@github.com:SRGSSR/pillarbox-android.git")
                            url.set("https://github.com/SRGSSR/pillarbox-android")
                        }

                        issueManagement {
                            system.set("GitHub Issues")
                            url.set("https://github.com/SRGSSR/pillarbox-android/issues")
                        }

                        ciManagement {
                            system.set("GitHub Actions")
                            url.set("https://github.com/SRGSSR/pillarbox-android/actions")
                        }

                        // TODO Simplify this once https://github.com/gradle/gradle/issues/28759 is released
                        withXml {
                            asNode().appendNode("distributionManagement").apply {
                                appendNode("repository").apply {
                                    appendNode("id", "github")
                                    appendNode("name", "GitHub Packages")
                                    appendNode("url", "https://maven.pkg.github.com/SRGSSR/pillarbox-android")
                                }
                            }
                        }
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
                maven {
                    name = "SRGSSRMaven"
                    url = uri("https://nxrm.rts.ch/repository/maven-srgssr-releases/")

                    credentials {
                        username = providers.gradleProperty("sonatypeUsername")
                            .orElse(providers.environmentVariable("SONATYPE_USERNAME"))
                            .orNull
                        password = providers.gradleProperty("sonatypePassword")
                            .orElse(providers.environmentVariable("SONATYPE_PASSWORD"))
                            .orNull
                    }
                }
            }
        }

        extensions.configure<DokkaExtension> {
            dokkaSourceSets.getByName("main") {
                includes.from("docs/README.md")

                externalDocumentationLinks.register("kotlinx.coroutines") {
                    url.set(URI("https://kotlinlang.org/api/kotlinx.coroutines"))
                    packageListUrl.set(URI("https://kotlinlang.org/api/kotlinx.coroutines/package-list"))
                }

                externalDocumentationLinks.register("kotlinx.serialization") {
                    url.set(URI("https://kotlinlang.org/api/kotlinx.serialization"))
                    packageListUrl.set(URI("https://kotlinlang.org/api/kotlinx.serialization/package-list"))
                }

                // This is currently broken in Dokka for Android modules. See: https://github.com/Kotlin/dokka/issues/2876
                sourceLink {
                    val version = VersionConfig().versionName(default = name)

                    localDirectory.set(projectDir.resolve("src"))
                    remoteUrl.set(URI("https://github.com/SRGSSR/pillarbox-android/tree/$version/${target.name}/src"))
                }
            }
        }
    }
}
