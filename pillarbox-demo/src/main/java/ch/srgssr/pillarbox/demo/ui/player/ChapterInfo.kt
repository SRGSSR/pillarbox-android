/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.MediaMetadataView
import ch.srgssr.pillarbox.player.asset.Chapter
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.player.getCurrentChapterAsFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Display chapter information when player reach a new Chapter.
 */
@Composable
internal fun ChapterInfo(
    player: Player,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    visibilityDelay: Duration = 5.seconds,
) {
    val currentChapterFlow: Flow<Chapter?> = remember(player) {
        player.getCurrentChapterAsFlow()
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
            MediaMetadataView(mediaMetadata = it.mediaMetadata)
        }
    }
}
