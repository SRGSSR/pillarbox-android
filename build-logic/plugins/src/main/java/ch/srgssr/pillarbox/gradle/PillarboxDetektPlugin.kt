/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle

import ch.srgssr.pillarbox.gradle.internal.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Custom Gradle plugin to configure Detekt for Pillarbox.
 *
 * Check [Detekt's documentation](https://detekt.dev/docs/gettingstarted/gradle) for more information.
 */
class PillarboxDetektPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.gitlab.arturbosch.detekt")
        extensions.configure<DetektExtension> {
            autoCorrect = true
            basePath = rootDir.absolutePath
            buildUponDefaultConfig = true
            config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
            ignoredBuildTypes = listOf("release")
            parallel = true
        }
        dependencies {
            add("detekt", libs.findLibrary("detekt-cli").get())
            add("detektPlugins", libs.findLibrary("detekt-formatting").get())
        }

    }
}
