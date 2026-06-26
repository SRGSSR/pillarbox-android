/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

import com.android.build.api.dsl.CommonExtension
import java.io.File

internal fun CommonExtension.configureJava() = with(compileOptions) {
    sourceCompatibility = AppConfig.javaVersion
    targetCompatibility = AppConfig.javaVersion
}

internal fun CommonExtension.configureBuildFeatures() = with(buildFeatures) {
    resValues = false
    shaders = false
}

internal fun CommonExtension.configureLint(sarifOutputDir: File?) = with(lint) {
    abortOnError = true
    checkAllWarnings = true
    checkDependencies = true
    sarifReport = true
    sarifOutput = sarifOutput
    disable.add("LogConditional")
}
