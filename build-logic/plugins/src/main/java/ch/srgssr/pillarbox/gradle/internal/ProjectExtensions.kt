/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

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
        // coreLibraryDesugaring(libs.findLibrary("android-desugar-jdk-libs").get())
        add("coreLibraryDesugaring", libs.findLibrary("android-desugar-jdk-libs").get())
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
