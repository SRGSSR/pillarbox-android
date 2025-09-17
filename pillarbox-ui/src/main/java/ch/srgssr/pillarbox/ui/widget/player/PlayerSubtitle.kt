/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.PresentationState
import androidx.media3.ui.compose.state.rememberPresentationState
import ch.srgssr.pillarbox.ui.exoplayer.SubtitleTextSize
import ch.srgssr.pillarbox.ui.exoplayer.setTextSize

/**
 * A smart Composable function to display subtitles that are always visible even when the player surface is bigger than the view bounds.
 *
 * @param player The [Player] instance to retrieve subtitle cues from.
 * @param videoContentScale The [ContentScale] applied to the video content.
 * @param modifier The [Modifier] to apply to this layout.
 * @param presentationState The [PresentationState] to be used.
 * @param captionStyle Optional [CaptionStyleCompat] to override the user's preferred caption style.
 * @param subtitleTextSize Optional [SubtitleTextSize] to override the user's preferred subtitle text size.
 *
 * @see rememberPresentationState
 */
@Composable
fun PlayerSubtitle(
    player: Player?,
    videoContentScale: ContentScale,
    modifier: Modifier = Modifier,
    presentationState: PresentationState = rememberPresentationState(player),
    captionStyle: CaptionStyleCompat? = null,
    subtitleTextSize: SubtitleTextSize? = null
) {
    val textContentScale = when (videoContentScale) {
        ContentScale.Crop, ContentScale.FillHeight, ContentScale.FillWidth -> ContentScale.FillBounds
        else -> videoContentScale
    }
    val textModifier = modifier.resizeWithContentScale(contentScale = textContentScale, presentationState.videoSizeDp)
    player?.let {
        PlayerSubtitle(player = it, modifier = textModifier, captionStyle = captionStyle, subtitleTextSize = subtitleTextSize)
    }
}

/**
 * A Composable function that displays an ExoPlayer [SubtitleView].
 * It observes the active cues from the provided [player] and displays them in a [SubtitleView].
 *
 * @param player The [Player] instance to retrieve subtitle cues from.
 * @param modifier The [Modifier] to apply to this layout.
 * @param captionStyle Optional [CaptionStyleCompat] to override the user's preferred caption style.
 * @param subtitleTextSize Optional [SubtitleTextSize] to override the user's preferred subtitle text size.
 */
@Composable
fun PlayerSubtitle(
    player: Player?,
    modifier: Modifier = Modifier,
    captionStyle: CaptionStyleCompat? = null,
    subtitleTextSize: SubtitleTextSize? = null
) {
    var view by remember { mutableStateOf<SubtitleView?>(null) }
    AndroidView(
        modifier = modifier,
        factory = {
            SubtitleView(it)
        },
        onReset = {},
        update = { subtitleView ->
            captionStyle?.let { subtitleView.setStyle(it) } ?: subtitleView.setUserDefaultStyle()
            subtitleView.setTextSize(subtitleTextSize)
            view = subtitleView
        }
    )

    view?.let { view ->
        LaunchedEffect(view, player) {
            view.setCues(player?.currentCues?.cues)
            player?.listen {
                if (it.contains(Player.EVENT_CUES)) {
                    view.setCues(currentCues.cues)
                }
            }
        }
    }
}
