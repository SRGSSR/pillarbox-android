/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.exoplayer

import android.annotation.SuppressLint
import android.graphics.Color
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.Dimension
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import com.google.common.collect.ImmutableList

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
fun ExoPlayerSubtitleView(
    player: Player,
    modifier: Modifier = Modifier,
    captionStyle: CaptionStyleCompat? = null,
    subtitleTextSize: SubtitleTextSize? = null
) {
    val cues = rememberCues(player = player)
    ExoPlayerSubtitleView(modifier = modifier, cues = cues, captionStyle = captionStyle, subtitleTextSize = subtitleTextSize)
}

/**
 * A Composable function that displays an ExoPlayer [SubtitleView].
 *
 * @param modifier The [Modifier] to apply to this layout.
 * @param cues The list of cues to be displayed.
 * @param captionStyle Optional [CaptionStyleCompat] to override the user's preferred caption style.
 * @param subtitleTextSize Optional [SubtitleTextSize] to override the user's preferred subtitle text size.
 *
 * @see Player.getCurrentCues To get the current cues from the [Player].
 */
@Composable
fun ExoPlayerSubtitleView(
    modifier: Modifier = Modifier,
    cues: List<Cue>? = null,
    captionStyle: CaptionStyleCompat? = null,
    subtitleTextSize: SubtitleTextSize? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SubtitleView(context).apply {
                val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                layoutParams = lp
            }
        },
        update = { view ->
            view.setCues(cues)
            captionStyle?.let { view.setStyle(it) } ?: view.setUserDefaultStyle()
            when (subtitleTextSize) {
                is SubtitleTextSize.Fixed -> {
                    view.setFixedTextSize(subtitleTextSize.unit, subtitleTextSize.size)
                }

                is SubtitleTextSize.Fractional -> {
                    view.setFractionalTextSize(subtitleTextSize.fractionOfHeight, subtitleTextSize.ignorePadding)
                }

                else -> {
                    view.setUserDefaultTextSize()
                }
            }
        },
        onRelease = { view ->
            view.setCues(null)
        },
        onReset = { view ->
            view.setCues(null)
        },
    )
}

// cues is an ImmutableList
@SuppressLint("MutableCollectionMutableState")
@Composable
private fun rememberCues(player: Player): List<Cue> {
    var cues by remember(player) {
        mutableStateOf(player.currentCues.cues)
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onCues(cueGroup: CueGroup) {
                cues = cueGroup.cues
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            cues = ImmutableList.of()
        }
    }
    return cues
}

@Preview
@Composable
private fun PreviewSubtitleViewDefault() {
    ExoPlayerSubtitleView(
        modifier = Modifier,
        cues = listOf(Cue.Builder().setText("Default").build())
    )
}

@Preview
@Composable
private fun PreviewSubtitleViewFractionalHeight() {
    ExoPlayerSubtitleView(
        modifier = Modifier,
        cues = listOf(Cue.Builder().setText("Fractional").build()),
        subtitleTextSize = SubtitleTextSize.Fractional(0.25f)
    )
}

@Preview
@Composable
private fun PreviewSubtitleViewFixedSize() {
    ExoPlayerSubtitleView(
        modifier = Modifier,
        cues = listOf(Cue.Builder().setText("Fixed Size").build()),
        subtitleTextSize = SubtitleTextSize.Fixed(unit = Dimension.SP, 24f)
    )
}

@Preview
@Composable
private fun PreviewSubtitleViewStyled() {
    val style = CaptionStyleCompat(
        Color.GREEN, // ForegroundColor
        Color.DKGRAY, // BackgroundColor
        Color.BLACK, // WindowColor
        CaptionStyleCompat.EDGE_TYPE_RAISED,
        Color.WHITE, // EdgeColor
        null, // Typerface
    )
    ExoPlayerSubtitleView(
        modifier = Modifier,
        cues = listOf(Cue.Builder().setText("Styled").build()),
        captionStyle = style
    )
}
