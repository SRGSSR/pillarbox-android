/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureAndroidModule(extension: CommonExtension<*, *, *, *, *, *>) = with(extension) {
    namespace = "ch.srgssr.pillarbox." + name.removePrefix("pillarbox-").replace('-', '.')
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
    }

    compileOptions {
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        resValues = false
        shaders = false
    }

    dependencies {
        // coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
        add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.4")
    }
}

internal fun Project.configureKotlinModule() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(AppConfig.jvmTarget)
        }
    }
}

internal fun Project.configureAndroidLintModule(extension: CommonExtension<*, *, *, *, *, *>) = with(extension) {
    lint {
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = true
        sarifReport = true
        sarifOutput = file("${rootProject.rootDir}/build/reports/android-lint/$name.sarif")
        disable.add("LogConditional")
    }
}
