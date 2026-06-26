/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

import com.android.build.api.dsl.CommonExtension

internal fun CommonExtension.configureJava(){
    compileOptions.sourceCompatibility = AppConfig.javaVersion
    compileOptions.targetCompatibility = AppConfig.javaVersion
}

internal fun CommonExtension.configureBuildFeatures(){
    buildFeatures.resValues = false
    buildFeatures.shaders = false
}
