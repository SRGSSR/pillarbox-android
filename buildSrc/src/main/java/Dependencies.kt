/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

// https://developer.android.com/jetpack/androidx/explorer
// https://developer.android.com/jetpack/androidx/releases/compose-kotlin
object Version {
    const val composeCompiler = "1.5.1"
    const val detetk = "1.22.0"
}

object Dependencies {

    object Detekt {
        const val detektCli = "io.gitlab.arturbosch.detekt:detekt-cli:${Version.detetk}"
        const val detektFormatting = "io.gitlab.arturbosch.detekt:detekt-formatting:${Version.detetk}"
    }
}
