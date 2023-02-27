/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import android.annotation.SuppressLint
import android.widget.FrameLayout.LayoutParams
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
import androidx.media3.ui.SubtitleView
import com.google.common.collect.ImmutableList

/**
 * Composable basic version of [ExoPlayerSubtitleView] from Media3 (Exoplayer) that listen to Player Cues
 *
 * @param player The Player to get Cues
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun ExoPlayerSubtitleView(player: Player, modifier: Modifier = Modifier) {
    val cues = rememberCues(player = player)
    ExoPlayerSubtitleView(modifier = modifier, cues = cues)
}

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
            SubtitleView(context).apply {
                val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                layoutParams = lp
                setUserDefaultStyle()
                setUserDefaultTextSize()
            }
        }, update = { view ->
            view.setCues(cues)
        }
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
private fun PreviewSubtitleView() {
    ExoPlayerSubtitleView(modifier = Modifier, listOf(Cue.Builder().setText("Subtitle").build()))
}
