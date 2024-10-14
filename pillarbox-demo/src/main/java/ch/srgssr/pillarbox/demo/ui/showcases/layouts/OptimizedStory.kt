/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.player.playbackStateAsFlow
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.player.SurfaceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Optimized story-like layout.
 */
@Composable
fun OptimizedStory(storyViewModel: StoryViewModel = viewModel()) {
    val mediaItems = storyViewModel.mediaItems
    val pagerState = rememberPagerState { mediaItems.size }
    val settledPage by remember { derivedStateOf { pagerState.settledPage } }
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    LaunchedEffect(settledPage) {
        storyViewModel.play(storyViewModel.getPlayer(settledPage))
    }
    LaunchedEffect(currentPage) {
        storyViewModel.setCurrentPage(currentPage)
    }

    val movablePlayerView = remember {
        (0 until storyViewModel.playerCount).map { index ->
            movableContentOf {
                val player = remember { storyViewModel.getPlayer(index) }
                PlayerView(player, modifier = Modifier.fillMaxSize())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(0),
            ),
            beyondViewportPageCount = 0,
            state = pagerState,
        ) { page ->
            LaunchedEffect(page) {
                storyViewModel.setupPlayerForPage(page)
            }
            movablePlayerView[page % movablePlayerView.size]()
        }

        PagerIndicator(
            currentPage = settledPage,
            pageCount = mediaItems.size,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = MaterialTheme.paddings.small),
        )
    }
}

@Composable
private fun PlayerView(player: Player, modifier: Modifier = Modifier) {
    val progress by remember {
        player.currentPositionAsFlow(100.milliseconds)
            .map { it / player.duration.coerceAtLeast(1L).toFloat() }
    }.collectAsState(0f)

    val isBuffering by remember {
        player.playbackStateAsFlow().map { it == Player.STATE_BUFFERING }
    }.collectAsState(false)

    Box(
        modifier = modifier,
    ) {
        PlayerSurface(
            modifier = Modifier.fillMaxHeight(),
            scaleMode = ScaleMode.Crop,
            surfaceType = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) SurfaceType.Texture else SurfaceType.Surface,
            player = player,
            defaultAspectRatio = 9 / 16f,
        )

        if (isBuffering) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = PrimaryComponentColor,
            trackColor = SecondaryComponentColor,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun PagerIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = SurfaceComponentColor,
                shape = CircleShape,
            )
            .padding(MaterialTheme.paddings.micro),
    ) {
        repeat(pageCount) { index ->
            val dotColor by animateColorAsState(
                targetValue = if (currentPage == index) PrimaryComponentColor else SecondaryComponentColor,
                label = "indicator-animation",
            )

            Box(
                modifier = Modifier
                    .padding(MaterialTheme.paddings.micro)
                    .size(IndicatorSize)
                    .drawBehind {
                        drawCircle(dotColor)
                    },
            )
        }
    }
}

@Preview
@Composable
private fun PageIndicatorPreview() {
    val pageCount = 5
    var step by remember { mutableIntStateOf(1) }
    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentPage) {
        delay(1.seconds)
        currentPage += step

        if (currentPage == pageCount - 1) {
            step = -1
        } else if (currentPage == 0) {
            step = 1
        }
    }

    PillarboxTheme {
        PagerIndicator(
            currentPage = currentPage,
            pageCount = pageCount,
        )
    }
}

private val ComponentColor = Color.LightGray
private val PrimaryComponentColor = ComponentColor.copy(alpha = 0.88f)
private val SecondaryComponentColor = ComponentColor.copy(alpha = 0.33f)
private val SurfaceComponentColor = ComponentColor.copy(alpha = 0.25f)
private val IndicatorSize = 12.dp
