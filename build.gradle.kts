/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependency.analysis.gradle.plugin)
    alias(libs.plugins.kotlinx.kover)
}

apply(plugin = "android-reporting")

val detektReportMerge by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/pillarbox-android.sarif"))
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    // Official site : https://detekt.dev/docs/gettingstarted/gradle
    // Tutorial : https://medium.com/@nagendran.p/integrating-detekt-in-the-android-studio-442128e971f8
    detekt {
        config.setFrom(files("../config/detekt/detekt.yml"))
        // preconfigure defaults
        buildUponDefaultConfig = false
        ignoredBuildTypes = listOf("release")
        autoCorrect = true
        parallel = true
    }

    dependencies {
        detekt(libs.detekt.cli)
        detektPlugins(libs.detekt.formatting)
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
        basePath = rootDir.absolutePath
        reports {
            xml.required = false
            html.required = true
            txt.required = false
            sarif.required = true
            md.required = false
        }
        finalizedBy(detektReportMerge)
    }

    detektReportMerge {
        input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

/*
 * https://detekt.dev/docs/gettingstarted/git-pre-commit-hook
 * https://medium.com/@alistair.cerio/android-ktlint-and-pre-commit-git-hook-5dd606e230a9
 */
tasks.register<Copy>("installGitHook") {
    description = "Install the Git pre-commit hook locally"
    from(file("${rootProject.rootDir}/git_hooks/pre-commit"))
    into { file("${rootProject.rootDir}/.git/hooks") }
    filePermissions {
        unix("rwxr-xr-x")
    }
}

tasks.getByPath(":pillarbox-demo:preBuild").dependsOn(":installGitHook")

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
        }

        structure {
            // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/wiki/Customizing-plugin-behavior
            ignoreKtx(true) // default is false

            // Required because of https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/892
            bundle("kotlin-test") {
                includeDependency(libs.kotlin.test)
            }
        }

        project(":pillarbox-core-business") {
            onUnusedDependencies {
                // This dependency is not used directly, but required to be able to compile `CommandersActStreaming`
                exclude(libs.tagcommander.core)
            }
        }

        project(":pillarbox-demo") {
            onUnusedDependencies {
                // This dependency is not used directly, but required to have previews in Android Studio
                exclude(libs.androidx.compose.ui.tooling.asProvider())
            }
        }

        project(":pillarbox-demo-tv") {
            onUnusedDependencies {
                // This dependency is not used directly, but required to have previews in Android Studio
                exclude(libs.androidx.compose.ui.tooling.asProvider())
            }
        }

        project(":pillarbox-player") {
            onUnusedDependencies {
                // These dependencies are not used directly, but automatically used by libs.androidx.media3.exoplayer
                exclude(libs.androidx.media3.dash, libs.androidx.media3.hls)
                // This dependency is used automatically by libs.mockk
                exclude(libs.mockk.android)
            }
        }

        project(":pillarbox-ui") {
            onUnusedDependencies {
                // This dependency is not used directly, but required to have previews in Android Studio
                exclude(libs.androidx.compose.ui.tooling.asProvider())
            }
        }
    }
}
