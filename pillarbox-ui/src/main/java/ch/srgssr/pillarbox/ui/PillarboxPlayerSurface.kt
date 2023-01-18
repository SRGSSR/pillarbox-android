/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import okhttp3.internal.toImmutableList

/**
 * Pillarbox player surface
 *
 * @param player The player to render in this SurfaceView
 * @param modifier The modifier to be applied to the layout.
 * @param scaleMode The scale mode to use.
 * @param contentAlignment The "letterboxing" content alignment inside the parent.
 * @param defaultAspectRatio The aspect ratio to use while video is loading or for audio content. 0.0f
 * @param content The Composable content to display on top of the Surface.
 */
@Composable
fun PillarboxPlayerSurface(
    player: Player?,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    defaultAspectRatio: Float = 0.0f,
    content: @Composable () -> Unit = {}
) {
    val playerSize = rememberPlayerSize(player = player)
    val cues = rememberCues(player = player)
    val videoAspectRatio = playerSize.computeAspectRatio(unknownAspectRatioValue = defaultAspectRatio)
    AspectRatioBox(
        modifier = modifier,
        aspectRatio = videoAspectRatio,
        scaleMode = scaleMode,
        contentAlignment = contentAlignment
    ) {
        PlayerSurface(player = player)
        if (cues.isNotEmpty()) {
            SubtitleView(cues = cues)
        }
        content.invoke()
    }
}

@Composable
private fun rememberPlayerSize(player: Player?): VideoSize {
    var playerSize by remember { mutableStateOf(player?.videoSize ?: VideoSize.UNKNOWN) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                playerSize = videoSize
            }
        }
        player?.addListener(listener)
        onDispose {
            player?.removeListener(listener)
        }
    }
    return playerSize
}

@Composable
private fun rememberCues(player: Player?): List<Cue> {
    if (player == null) {
        return emptyList()
    }
    var cues by remember {
        mutableStateOf(player.currentCues.cues.toImmutableList())
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onCues(cueGroup: CueGroup) {
                cues = cueGroup.cues.toImmutableList()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            cues = emptyList<Cue>()
        }
    }

    return cues
}
