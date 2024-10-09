/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.player.SurfaceType

/**
 * Optimized story trying to reproduce story-like TikTok or Instagram.
 *
 * Surface view may sometimes keep on screen. Maybe if we use TextView with PlayerView this strange behavior will disappear.
 */
@Composable
fun OptimizedStory(storyViewModel: StoryViewModel = viewModel()) {
    val pagerState = rememberPagerState {
        storyViewModel.playlist.items.size
    }
    LifecycleStartEffect(pagerState) {
        storyViewModel.getPlayerForPageNumber(pagerState.currentPage).play()

        onStopOrDispose {
            storyViewModel.pauseAllPlayer()
        }
    }

    val playlist = storyViewModel.playlist.items
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            beyondViewportPageCount = 0,
            key = { page -> playlist[page].uri },
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(0),
                snapAnimationSpec = spring(stiffness = Spring.StiffnessHigh)
            ),
            pageSpacing = 1.dp,
            state = pagerState
        ) { page ->
            // When flinging -> may "load" more that 3 pages
            val currentPage = pagerState.currentPage
            val player = if (page == currentPage - 1 || page == currentPage + 1 || page == currentPage) {
                val playerConfig = storyViewModel.getPlayerAndMediaItemIndexForPage(page)
                val playerPage = storyViewModel.getPlayerFromIndex(playerConfig.first)
                playerPage.playWhenReady = currentPage == page
                playerPage.seekToDefaultPosition(playerConfig.second)
                playerPage
            } else {
                null
            }
            player?.let {
                PlayerSurface(
                    modifier = Modifier.fillMaxHeight(),
                    scaleMode = ScaleMode.Crop,
                    // Using Texture instead of Surface because on Android API 34 animations are not working well due to the hack
                    // See PlayerSurfaceView in AndroidPlayerSurfaceView
                    surfaceType = SurfaceType.Texture,
                    player = player,
                )
            }
            Text(text = "Page $page")
        }
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = MaterialTheme.paddings.baseline),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(playlist.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) ColorIndicatorCurrent else ColorIndicator
                Box(
                    modifier = Modifier
                        .padding(MaterialTheme.paddings.micro)
                        .size(IndicatorSize)
                        .drawBehind {
                            drawCircle(color)
                        }

                )
            }
        }
    }
}

private val ColorIndicatorCurrent = Color.LightGray.copy(alpha = 0.75f)
private val ColorIndicator = Color.LightGray.copy(alpha = 0.25f)
private val IndicatorSize = 12.dp
