/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerNoContent
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.BlockedInterval
import ch.srgssr.pillarbox.player.asset.Chapter
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.hasMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.ToggleableBox
import ch.srgssr.pillarbox.ui.widget.keepScreenOn
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState
import coil.compose.AsyncImage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.time.Duration.Companion.seconds

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
    val isSliderDragged by interactionSource.collectIsDraggedAsState()
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
        val currentChapterFlow: Flow<Chapter?> = remember(player) {
            callbackFlow {
                val listener = object : PillarboxPlayer.Listener {
                    override fun onCurrentChapterChanged(chapter: Chapter?) {
                        trySend(chapter)

                    }
                }
                player.addListener(listener)
                awaitClose {
                    player.removeListener(listener)
                }
            }
        }
        val currentChapter by currentChapterFlow.collectAsState(initial = player.getChapterAtPosition())
        var chapterInfoVisibility by remember {
            mutableStateOf(currentChapter != null)
        }
        LaunchedEffect(currentChapter) {
            chapterInfoVisibility = currentChapter != null
            if (chapterInfoVisibility) {
                delay(5.seconds)
                chapterInfoVisibility = false
            }
        }
        PlayerSurface(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            player = player,
            scaleMode = scaleMode
        ) {
            if (isBuffering && !isSliderDragged) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
            }
            ExoPlayerSubtitleView(player = player)
        }
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(2f),
            visible = chapterInfoVisibility && !visibilityState.isVisible
        ) {
            currentChapter?.let {
                ChapterInfoView(chapter = it)
            }
        }
    }
}

@Composable
private fun ChapterInfoView(chapter: Chapter, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(color = Color.Black.copy(alpha = 0.5f))
            .padding(MaterialTheme.paddings.baseline)
    ) {
        AsyncImage(
            model = chapter.mediaMetadata.artworkUri,
            contentDescription = null
        )
        Text(
            text = chapter.mediaMetadata.title.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            overflow = TextOverflow.Ellipsis
        )
        chapter.mediaMetadata.description?.let {
            Text(text = chapter.mediaMetadata.description.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Preview
@Composable
private fun ChapterInfoPreview() {
    MaterialTheme {
        ChapterInfoView(
            chapter = Chapter(
                "id", 0, 0,
                MediaMetadata.Builder()
                    .setTitle("Chapter title")
                    .setDescription("Chapter description")
                    .build()
            )
        )
    }
}

