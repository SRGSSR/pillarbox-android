/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.configureAndroidModule(extension: CommonExtension<*, *, *, *, *, *>) = with(extension) {
    namespace = "ch.srgssr.pillarbox." + name.removePrefix("pillarbox-").replace('-', '.')
    compileSdk = AppConfig.compileSdk

    compileOptions {
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }

    buildFeatures {
        resValues = false
        shaders = false
    }
}

internal fun Project.configureKotlinModule() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(AppConfig.jvmTarget)
            optIn.add("kotlin.time.ExperimentalTime") // TODO Remove once kotlin.time.Clock and kotlin.time.Instant are not longer experimental
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

internal fun Project.configureAndroidLintModule(extension: CommonExtension<*, *, *, *, *, *>) = with(extension) {
    lint {
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = true
        sarifReport = true
        sarifOutput = rootProject.projectDir.resolve("build/reports/android-lint/$name.sarif")
        disable.add("LogConditional")
    }
}
