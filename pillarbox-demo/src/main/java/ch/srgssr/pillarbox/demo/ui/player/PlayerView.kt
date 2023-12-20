/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerNoContent
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.hasMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.ToggleableBox
import ch.srgssr.pillarbox.ui.widget.keepScreenOn
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState

/**
 * Simple player view
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param scaleMode The surface scale mode.
 * @param controlsVisible The control visibility.
 * @param controlsToggleable The controls are toggleable.
 * @param content The action to display under the slider.
 */
@Composable
fun PlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    controlsVisible: Boolean = true,
    controlsToggleable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val playerError by player.playerErrorAsState()
    playerError?.let {
        PlayerError(
            modifier = modifier,
            playerError = it,
            onRetry = player::prepare
        )
        return
    }

    val hasMediaItem by player.hasMediaItemsAsState()
    if (!hasMediaItem) {
        PlayerNoContent(modifier = modifier)
        return
    }
    player.keepScreenOn()
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isSliderDragged = interactionSource.collectIsDraggedAsState().value
    val visibilityState = rememberDelayedVisibilityState(
        player = player,
        autoHideEnabled = !isSliderDragged,
        visible = controlsVisible
    )
    ToggleableBox(
        modifier = modifier,
        toggleable = controlsToggleable,
        visibilityState = visibilityState,
        toggleableContent = {
            PlayerControls(
                player = player,
                interactionSource = interactionSource,
                content = content
            )
        }
    ) {
        val playbackState by player.playbackStateAsState()
        val isBuffering = playbackState == Player.STATE_BUFFERING
        PlayerSurface(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            player = player,
            scaleMode = scaleMode
        ) {
            if (isBuffering) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
            }
            ExoPlayerSubtitleView(player = player)
        }
    }
}
