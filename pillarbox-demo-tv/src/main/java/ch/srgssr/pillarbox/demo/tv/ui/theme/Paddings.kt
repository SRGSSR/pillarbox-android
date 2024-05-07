/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme

/**
 * Stores all the paddings used in this demo application.
 *
 * @property baseline
 * @property small
 * @property mini
 */
@Immutable
class Paddings(
    val baseline: Dp = 16.dp,
    val small: Dp = 8.dp,
    val mini: Dp = 4.dp,
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
