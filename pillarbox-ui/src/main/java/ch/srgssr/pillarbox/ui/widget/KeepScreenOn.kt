/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.extension.isPlayingAsState

/**
 * Keep screen on when [Player.isPlaying]
 */
@Composable
fun Player.keepScreenOn() {
    val isPlaying by isPlayingAsState()
    LocalView.current.keepScreenOn = isPlaying
}
