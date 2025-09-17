/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.Player
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.PresentationState
import androidx.media3.ui.compose.state.rememberPresentationState

/**
 * Provides a surface for a [Player].
 *
 * @param player The [Player] to be displayed.
 * @param modifier The [Modifier] to be applied to the surface.
 * @param contentScale The [ContentScale] to be applied to the surface.
 * @param surfaceType The type of surface to be used.
 * @param displayDebugView Whether to display a debug view.
 * @param presentationState The [PresentationState] to be used.
 * @param surface A composable function that draws on top of the surface. It may be displayed outside the bounds.
 * @param subtitle A composable function that draws the subtitle.
 * @param shutter A composable function that draws the shutter when the player hasn't active video tracks.
 */
@Composable
fun PlayerFrame(
    player: Player?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    surfaceType: SurfaceType = SurfaceType.Surface,
    displayDebugView: Boolean = true,
    presentationState: PresentationState = rememberPresentationState(player = player, keepContentOnReset = false),
    surface: (@Composable BoxScope.() -> Unit)? = null,
    subtitle: @Composable () -> Unit = {
        PlayerSubtitle(
            modifier = Modifier,
            player = player,
            presentationState = presentationState,
            videoContentScale = contentScale
        )
    },
    shutter: @Composable () -> Unit = {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    },
) {
    Box(
        modifier = modifier
            .clipToBounds()
            .resizeWithContentScale(contentScale = contentScale, sourceSizeDp = presentationState.videoSizeDp)
    ) {
        PillarboxPlayerSurface(player = player, surfaceType = surfaceType, modifier = Modifier.fillMaxSize())
        surface?.invoke(this)
        if (displayDebugView) {
            DebugPlayerView(Modifier.fillMaxSize())
        }
    }
    subtitle()

    if (presentationState.coverSurface) {
        shutter()
    }
}
