/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import coil3.compose.AsyncImage
import kotlin.time.Duration.Companion.minutes

/**
 * Chapter showcase
 * - Display a chapter list below the player.
 *
 * @param modifier The [Modifier] to apply to this layout.
 */
@Composable
fun ChapterShowcase(modifier: Modifier = Modifier) {
    val showCaseViewModel: ChaptersShowcaseViewModel = viewModel()
    val demoItem = showCaseViewModel.demoItem
    val chapters by showCaseViewModel.chapters.collectAsState()
    val currentChapter by showCaseViewModel.currentChapter.collectAsState()
    val configuration = LocalConfiguration.current
    Column(modifier = modifier) {
        PlayerView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .weight(1f),
            player = showCaseViewModel.player,
            progressTracker = showCaseViewModel.progressTracker
        )
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            visible = chapters.isNotEmpty() && configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        ) {
            ChapterList(
                chapters = chapters,
                demoItem = demoItem,
                currentChapter = currentChapter,
                onChapterClick = showCaseViewModel::chapterClicked,
            )
        }
    }
}

private const val CurrentItemOffset = -64

@Composable
private fun ChapterList(
    chapters: List<Chapter>,
    demoItem: DemoItem,
    modifier: Modifier = Modifier,
    currentChapter: Chapter? = null,
    onChapterClick: (Chapter) -> Unit = {}
) {
    val currentIndex = currentChapter?.let { chapters.indexOf(it) } ?: -1
    val state = rememberLazyListState(if (currentIndex >= 0) currentIndex else 0, CurrentItemOffset)
    LaunchedEffect(currentIndex, chapters) {
        if (currentIndex >= 0) {
            state.animateScrollToItem(currentIndex, if (currentIndex != 0) CurrentItemOffset else 0)
        }
    }
    LazyRow(
        modifier = modifier.semantics {
            collectionInfo = CollectionInfo(rowCount = chapters.size, columnCount = 1)
        },
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
        contentPadding = PaddingValues(MaterialTheme.paddings.small),
        state = state
    ) {
        itemsIndexed(
            items = chapters,
            key = { _, chapter -> chapter.id },
        ) { index, chapter ->
            ChapterItem(
                modifier = Modifier
                    .aspectRatio(16 / 9f)
                    .semantics {
                        collectionItemInfo = CollectionItemInfo(
                            rowIndex = index,
                            rowSpan = 1,
                            columnIndex = 1,
                            columnSpan = 1,
                        )
                    },
                chapter = chapter,
                demoItem = demoItem,
                active = currentChapter == chapter,
                onClick = { onChapterClick(chapter) }
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    active: Boolean,
    demoItem: DemoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zIndex by animateFloatAsState(targetValue = if (active) 1f else 0f, label = "zIndex")
    val scale by animateFloatAsState(targetValue = if (active) 1.05f else 0.95f, label = "scale")
    val imageAlpha by animateFloatAsState(targetValue = if (active) 0.2f else 0.5f, label = "image_alpha")
    Box(
        modifier = modifier
            .zIndex(zIndex)
            .scale(scale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val placeholder = rememberVectorPainter(image = Icons.Default.Image)
        val localeList = demoItem.languageTag?.let { LocaleList(Locale(it)) }

        AsyncImage(
            model = chapter.mediaMetadata.artworkUri,
            contentDescription = "",
            placeholder = placeholder,
            fallback = placeholder,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(color = Color.Black.copy(alpha = imageAlpha), blendMode = BlendMode.SrcOver),
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(MaterialTheme.paddings.small),
            maxLines = 2,
            minLines = 2,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            text = buildAnnotatedString {
                withStyle(SpanStyle(localeList = localeList)) {
                    append(chapter.mediaMetadata.title)
                }
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (active) FontWeight.Bold else null,
            color = Color.White,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChapterItemPreview() {
    PillarboxTheme {
        ChapterItem(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f),
            chapter = Chapter(
                id = "i1",
                start = 5.minutes.inWholeMilliseconds,
                end = 12.minutes.inWholeMilliseconds,
                mediaMetadata = MediaMetadata.Builder()
                    .setTitle("Title2")
                    .setArtworkUri(
                        """
                        https://cdn.prod.swi-services.ch/video-delivery/images/
                        14e4562f-725d-4e41-a200-7fcaa77df2fe/5rwf1Bq_m3GC5secOZcIcgbbrbZPf4nI/16x9
                        """.trimIndent().toUri()
                    )
                    .build()
            ),
            active = false,
            demoItem = SamplesSRG.OnDemandHorizontalVideo,
            onClick = {}
        )
    }
}
