/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependency.analysis.gradle.plugin)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.pillarbox.detekt)
}

allprojects {
    if (pluginManager.hasPlugin("org.jetbrains.dokka")) {
        dokka {
            moduleVersion = providers.environmentVariable("VERSION_NAME").orElse("dev")

            pluginsConfiguration.html {
                // See the overridable images here:
                // https://github.com/Kotlin/dokka/tree/master/dokka-subprojects/plugin-base/src/main/resources/dokka/images
                customAssets.from(rootProject.projectDir.resolve("config/dokka/images/logo-icon.svg")) // TODO Use Pillarbox logo
                customStyleSheets.from(rootProject.projectDir.resolve("config/dokka/styles/pillarbox.css"))
                footerMessage.set("© SRG SSR")
                homepageLink.set("https://www.pillarbox.ch/")
                templatesDir.set(rootProject.projectDir.resolve("config/dokka/templates"))
                separateInheritedMembers = true
            }
        }
    }
}

dokka {
    dokkaPublications.html {
        includes.from("config/dokka/Pillarbox.md")
    }
}

// Configure the `wrapper` task, so it can easily be updated by simply running `./gradlew wrapper`.
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "latest"
}

val clean by tasks.getting(Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

/*
 * https://detekt.dev/docs/gettingstarted/git-pre-commit-hook
 * https://medium.com/@alistair.cerio/android-ktlint-and-pre-commit-git-hook-5dd606e230a9
 */
val installGitHook by tasks.registering(Copy::class) {
    description = "Install the Git pre-commit hook locally"
    from(rootProject.projectDir.resolve("config/git/pre-commit"))
    into { rootProject.projectDir.resolve(".git/hooks") }
    filePermissions {
        unix("rwxr-xr-x")
    }
}

tasks.getByPath(":pillarbox-demo:preBuild").dependsOn(installGitHook)

dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                severity("fail")
                exclude(libs.androidx.compose.ui.tooling.asProvider())
            }
        }

        project(":pillarbox-core-business") {
            onUnusedDependencies {
                // This dependency is not used directly, but required to be able to compile `CommandersActStreaming`
                exclude(libs.tagcommander.core)
            }
        }

        project(":pillarbox-core-business-cast") {
            onUnusedDependencies {
                // This dependency is not used directly, but needed to get Cast dependencies
                exclude(":pillarbox-cast")
            }
        }

        project(":pillarbox-player") {
            onUnusedDependencies {
                // These dependencies are not used directly, but automatically used by libs.androidx.media3.exoplayer
                exclude(libs.androidx.media3.dash, libs.androidx.media3.hls)
                exclude(libs.okio)
            }

            onUsedTransitiveDependencies {
                exclude(libs.okio)
            }
        }
    }
}
