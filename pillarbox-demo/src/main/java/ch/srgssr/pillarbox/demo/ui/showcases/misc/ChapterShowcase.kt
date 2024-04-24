/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.asset.Chapter
import coil.compose.AsyncImage
import kotlin.time.Duration.Companion.minutes

/**
 * Chapter showcase
 * - Display a chapter list above the player.
 */
@Composable
fun ChapterShowcase() {
    val showCaseViewModel: ChaptersShowcaseViewModel = viewModel()
    val chapters by showCaseViewModel.chapters.collectAsState()
    val currentChapter by showCaseViewModel.currentChapter.collectAsState()
    val configuration = LocalConfiguration.current
    Column {
        PlayerView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .weight(4f),
            player = showCaseViewModel.player,
            progressTracker = showCaseViewModel.progressTracker
        )
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            visible = chapters.isNotEmpty() && configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        ) {
            ChapterList(
                chapters = chapters,
                currentChapter = currentChapter,
                onChapterClicked = showCaseViewModel::chapterClicked,
            )
        }
    }
}

private const val CurrentItemOffset = -64

@Composable
private fun ChapterList(
    chapters: List<Chapter>,
    modifier: Modifier = Modifier,
    currentChapter: Chapter? = null,
    onChapterClicked: (Chapter) -> Unit = {}
) {
    val currentIndex = currentChapter?.let { chapters.indexOf(it) } ?: 0
    val state = rememberLazyListState(currentIndex, CurrentItemOffset)
    LaunchedEffect(currentIndex, chapters) {
        currentChapter?.let {
            state.animateScrollToItem(currentIndex, CurrentItemOffset)
        }
    }
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini),
        contentPadding = PaddingValues(MaterialTheme.paddings.mini),
        state = state
    ) {
        items(items = chapters, key = { it.id }) { chapter ->
            ChapterItem(
                modifier = Modifier
                    .fillParentMaxHeight()
                    .aspectRatio(16 / 10f),
                chapter = chapter, active = currentChapter == chapter, onClick = { onChapterClicked(chapter) }
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val placeholder = rememberVectorPainter(image = Icons.Default.ImageNotSupported)
        AsyncImage(
            model = chapter.mediaMetadata.artworkUri,
            contentDescription = "",
            placeholder = placeholder,
            fallback = placeholder,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = if (active) Color.Red else Color.Black)
                .padding(MaterialTheme.paddings.micro)
                .align(Alignment.BottomStart),
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            text = chapter.mediaMetadata.title.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChapterItemPreview() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            val modifier = Modifier
                .fillMaxWidth(0.75f)
                .aspectRatio(16 / 9f)
            ChapterItem(
                modifier = modifier,
                chapter = Chapter(
                    "i1", 5.minutes.inWholeMilliseconds, 10.minutes.inWholeMilliseconds,
                    MediaMetadata.Builder()
                        .setTitle("Title1")
                        .build()
                ),
                active = true,
                onClick = {}
            )
            ChapterItem(
                modifier = modifier,
                chapter = Chapter(
                    "i1", 5.minutes.inWholeMilliseconds, 12.minutes.inWholeMilliseconds,
                    MediaMetadata.Builder()
                        .setTitle("Title2")
                        .setArtworkUri(
                            Uri.parse(
                                "https://cdn.prod.swi-services" +
                                    ".ch/video-delivery/images/14e4562f-725d-4e41-a200-7fcaa77df2fe/5rwf1Bq_m3GC5secOZcIcgbbrbZPf4nI/16x9"
                            )
                        )
                        .build()
                ),
                active = false,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun ChapterListPreview() {
    val listChapters = listOf(
        Chapter(
            "i1", 5.minutes.inWholeMilliseconds, 10.minutes.inWholeMilliseconds,
            MediaMetadata.Builder()
                .setTitle("Title1")
                .build()
        ),
        Chapter(
            "i2", 5.minutes.inWholeMilliseconds, 10.minutes.inWholeMilliseconds,
            MediaMetadata.Builder()
                .setTitle("Title 2 that can be a long as needed but shorter as required!")
                .build()
        ),
        Chapter(
            "i3", 5.minutes.inWholeMilliseconds, 10.minutes.inWholeMilliseconds,
            MediaMetadata.Builder()
                .setTitle("Title 3")
                .build()
        ),
    )
    MaterialTheme {
        ChapterList(
            modifier = Modifier
                .height(150.dp),
            chapters = listChapters
        )
    }
}
