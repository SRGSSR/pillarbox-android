/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlinx.kover)
}

group = "ch.srgssr.pillarbox.gradle"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.dokka.gradle.plugin)
    compileOnly(libs.kotlinx.kover.gradle)
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(libs.kotlin.test)
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

        register("PillarboxDetekt") {
            id = "ch.srgssr.pillarbox.gradle.detekt"
            implementationClass = "ch.srgssr.pillarbox.gradle.PillarboxDetektPlugin"
        }
    }
}
