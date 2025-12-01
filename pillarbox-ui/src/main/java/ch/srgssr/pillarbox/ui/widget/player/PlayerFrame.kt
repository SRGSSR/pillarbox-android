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
import androidx.compose.runtime.remember
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
 * @param subtitle A composable function that draws the subtitle. Subtitle can only [SubtitleContentScale.Fill] or [SubtitleContentScale.Fill].
 * @param shutter A composable function that draws when [PresentationState.coverSurface] is true.
 * @param overlay A composable function that draws on top of everything including [shutter] and [subtitle].
 */
@Composable
fun PlayerFrame(
    player: Player?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    surfaceType: SurfaceType = SurfaceType.Surface,
    displayDebugView: Boolean = false,
    presentationState: PresentationState = rememberPresentationState(player = player, keepContentOnReset = false),
    surface: (@Composable BoxScope.() -> Unit)? = null,
    subtitle: @Composable SubtitleBoxScope.() -> Unit = {
        PlayerSubtitle(
            modifier = Modifier,
            player = player,
        )
    },
    shutter: @Composable BoxScope.() -> Unit = {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    },
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.clipToBounds()) {
        Box(
            modifier = Modifier.resizeWithContentScale(contentScale = contentScale, sourceSizeDp = presentationState.videoSizeDp)
        ) {
            PillarboxPlayerSurface(player = player, surfaceType = surfaceType, modifier = Modifier.fillMaxSize())
            surface?.invoke(this)
            if (displayDebugView) {
                DebugPlayerView(Modifier.fillMaxSize())
            }
        }
        if (presentationState.coverSurface) {
            shutter()
        }

        val subtitleContentScale = contentScale.toSubtitleContentScale()
        val subtitleModifier = Modifier.resizeWithContentScale(contentScale = subtitleContentScale.contentScale, presentationState.videoSizeDp)
        Box(modifier = subtitleModifier) {
            val subtitleScope = remember(subtitleContentScale) {
                SubtitleBoxScope(contentScale = subtitleContentScale, boxScope = this)
            }
            subtitleScope.subtitle()
        }
        overlay()
    }
}

/**
 * A [BoxScope] with the [SubtitleContentScale] applied to the scope.
 * @property contentScale The [SubtitleContentScale].
 */
class SubtitleBoxScope internal constructor(
    private val boxScope: BoxScope,
    val contentScale: SubtitleContentScale,
) : BoxScope by boxScope

/**
 * Content scale for Subtitles
 * @property contentScale The [ContentScale] associated.
 */
enum class SubtitleContentScale(val contentScale: ContentScale) {
    /**
     * A content scale that fit the video surface
     */
    Fit(ContentScale.Fit),

    /**
     * Content scale that fill the bounds.
     */
    Fill(ContentScale.FillBounds)
}

internal fun ContentScale.toSubtitleContentScale(): SubtitleContentScale {
    return when (this) {
        ContentScale.Fit -> SubtitleContentScale.Fit
        else -> SubtitleContentScale.Fill
    }
}
