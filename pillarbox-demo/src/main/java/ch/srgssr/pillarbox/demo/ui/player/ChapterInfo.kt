/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.Chapter
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import coil.compose.AsyncImage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Display chapter information when player reach a new Chapter.
 *
 * @param player
 * @param modifier
 * @param visible
 * @param visibilityDelay
 */
@Composable
fun ChapterInfo(
    player: Player,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    visibilityDelay: Duration = 5.seconds,
) {
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
            delay(visibilityDelay)
            chapterInfoVisibility = false
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = visible && chapterInfoVisibility
    ) {
        currentChapter?.let {
            ChapterInfo(chapter = it)
        }
    }
}

@Composable
private fun ChapterInfo(chapter: Chapter, modifier: Modifier = Modifier) {
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
        ChapterInfo(
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
