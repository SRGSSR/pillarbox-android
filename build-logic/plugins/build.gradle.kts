/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

plugins {
    `kotlin-dsl`
}

group = "ch.srgssr.pillarbox.gradle"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.kotlinx.kover.gradle)
    compileOnly(libs.kotlin.gradle.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("PillarboxAndroidApplication") {
            id = "ch.srgssr.pillarbox.gradle.android_application"
            implementationClass = "ch.srgssr.pillarbox.gradle.PillarboxAndroidApplicationPlugin"
        }

        register("PillarboxAndroidLibrary") {
            id = "ch.srgssr.pillarbox.gradle.android_library"
            implementationClass = "ch.srgssr.pillarbox.gradle.PillarboxAndroidLibraryPlugin"
        }

        register("PillarboxAndroidLibraryCompose") {
            id = "ch.srgssr.pillarbox.gradle.android_library_compose"
            implementationClass = "ch.srgssr.pillarbox.gradle.PillarboxAndroidLibraryComposePlugin"
        }

        register("PillarboxAndroidLibraryLint") {
            id = "ch.srgssr.pillarbox.gradle.android_library_lint"
            implementationClass = "ch.srgssr.pillarbox.gradle.PillarboxAndroidLibraryLintPlugin"
        }

        register("PillarboxAndroidLibraryPublishing") {
            id = "ch.srgssr.pillarbox.gradle.android_library_publishing"
            implementationClass = "ch.srgssr.pillarbox.gradle.PillarboxAndroidLibraryPublishingPlugin"
        }

        register("PillarboxAndroidLibraryTestedModule") {
            id = "ch.srgssr.pillarbox.gradle.android_library_tested_module"
            implementationClass = "ch.srgssr.pillarbox.gradle.PillarboxAndroidLibraryTestedModulePlugin"
        }
    }
}
