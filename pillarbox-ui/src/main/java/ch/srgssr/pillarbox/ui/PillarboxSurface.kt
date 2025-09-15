/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import android.os.Build
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.Player
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.PresentationState
import androidx.media3.ui.compose.state.rememberPresentationState
import ch.srgssr.pillarbox.ui.widget.player.AndroidSurfaceViewWithApi34WorkAround
import ch.srgssr.pillarbox.ui.widget.player.DebugPlayerView

/**
 * Remove the choice
 * Since minSDK = 24, we force to always use SURFACE_TYPE_SURFACE_VIEW.
 * https://developer.android.com/media/media3/ui/surface
 */
@Composable
fun PillarboxSurface(
    pillarboxPlayer: Player?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    displayDebugView: Boolean = true,
    keepContentOnReset: Boolean = false,
    presentationState: PresentationState = rememberPresentationState(player = pillarboxPlayer, keepContentOnReset = keepContentOnReset),
    surfaceContent: (@Composable BoxScope.() -> Unit)? = null,
    shutter: @Composable () -> Unit = {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    },
) {
    val scaledModifier = modifier.resizeWithContentScale(contentScale = contentScale, presentationState.videoSizeDp)

    // Always leave PlayerSurface to be part of the Compose tree because it will be initialized in
    // the process. If this composable is guarded by some condition, it might never become visible
    // because the Player will not emit the relevant event, e.g. the first frame being ready.
    // PlayerSurface(player = pillarboxPlayer, surfaceType = SURFACE_TYPE_SURFACE_VIEW, modifier = scaledModifier)
    Box(modifier = scaledModifier, contentAlignment = Alignment.Center) {
        PlayerSurfaceInternal(player = pillarboxPlayer)
        if (displayDebugView) {
            DebugPlayerView(Modifier.fillMaxSize())
        }
        surfaceContent?.invoke(this)
    }

    if (presentationState.coverSurface) {
        shutter()
    }
}

@Composable
private fun PlayerSurfaceInternal(player: Player?) {
    if (Build.VERSION.SDK_INT == UPSIDE_DOWN_CAKE) {
        // player?.let { AndroidPlayerSurfaceView(it) }
        AndroidSurfaceViewWithApi34WorkAround(player)
    } else {
        PlayerSurface(player = player, surfaceType = SURFACE_TYPE_SURFACE_VIEW)
    }
}
