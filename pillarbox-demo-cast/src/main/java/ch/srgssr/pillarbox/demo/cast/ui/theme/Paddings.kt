/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stores all the paddings used in this demo application.
 *
 * @property medium
 * @property baseline
 * @property small
 * @property mini
 * @property micro
 */
@Immutable
class Paddings(
    val medium: Dp = 32.dp,
    val baseline: Dp = 16.dp,
    val small: Dp = 8.dp,
    val mini: Dp = 4.dp,
    val micro: Dp = 2.dp
)

/**
 * Retrieves the current [Paddings] at the call site's position in the hierarchy.
 */
@Suppress("UnusedReceiverParameter")
val MaterialTheme.paddings: Paddings
    @Composable
    @ReadOnlyComposable
    get() = LocalPaddings.current

internal val LocalPaddings = staticCompositionLocalOf { Paddings() }
