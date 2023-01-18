/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.text.Cue
import androidx.media3.ui.SubtitleView

/**
 * Composable basic version of [ExoPlayerSubtitleView] from Media3 (Exoplayer)
 *
 * @param modifier The modifier to be applied to the layout.
 * @param cues The List of Cues received from a Player.
 */
@Composable
fun ExoPlayerSubtitleView(modifier: Modifier = Modifier, cues: List<Cue>? = null) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SubtitleView(context)
        }, update = { view ->
            view.setCues(cues)
        }
    )
}
