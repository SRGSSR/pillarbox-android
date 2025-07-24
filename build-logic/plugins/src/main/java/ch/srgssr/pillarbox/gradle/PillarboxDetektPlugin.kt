/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle

import ch.srgssr.pillarbox.gradle.internal.AppConfig
import ch.srgssr.pillarbox.gradle.internal.libs
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * Custom Gradle plugin to configure Detekt for Pillarbox.
 *
 * Check [Detekt's documentation](https://detekt.dev/docs/gettingstarted/gradle) for more information.
 */
class PillarboxDetektPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("android-reporting")

        val detektReportMerge = tasks.register<ReportMergeTask>("detektReportMerge") {
            output.set(rootProject.layout.buildDirectory.file("reports/detekt/pillarbox-android.sarif"))
        }

        allprojects {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val detektTasks = tasks.withType<Detekt>()

            detektTasks.configureEach {
                jvmTarget = AppConfig.jvmTarget.target

                reports {
                    html.required.set(true)
                    md.required.set(false)
                    sarif.required.set(true)
                    txt.required.set(false)
                    xml.required.set(false)
                }

                finalizedBy(detektReportMerge)
            }

            extensions.configure<DetektExtension> {
                autoCorrect = true
                basePath = rootDir.absolutePath
                buildUponDefaultConfig = true
                config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
                ignoredBuildTypes = listOf("release")
                parallel = true
            }

            detektReportMerge.configure {
                input.from(detektTasks.map { it.sarifReportFile })
            }

            dependencies.add("detekt", libs.findLibrary("detekt-cli").get())
            dependencies.add("detektPlugins", libs.findLibrary("detekt-formatting").get())
        }
    }
}
