/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations.story

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.ui.PlayerSurface
import ch.srgssr.pillarbox.ui.ScaleMode
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**
 * Optimized story trying to reproduce story like TikTok or Instagram.
 *
 * Surface view may sometimes keep on screen. Maybe if we use TextView with PlayerView this strange behavior will disappear.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun OptimizedStory(storyViewModel: StoryViewModel = viewModel()) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val pagerState = rememberPagerState()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                storyViewModel.getPlayerForPageNumber(pagerState.currentPage).play()
            } else if (event == Lifecycle.Event.ON_STOP) {
                storyViewModel.pauseAllPlayer()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val playlist = storyViewModel.playlist.items
    HorizontalPager(
        modifier = Modifier.fillMaxHeight(),
        key = { page -> playlist[page].uri },
        count = playlist.size,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 1.dp) // Add tiny padding to remove surface glitches
    ) { page ->
        // When flinging -> may "load" more that 3 pages
        val player = if (page == currentPage - 1 || page == currentPage + 1 || page == currentPage) {
            val playerConfig = storyViewModel.getPlayerAndMediaItemIndexForPage(page)
            val playerPage = storyViewModel.getPlayerFromIndex(playerConfig.first)
            playerPage.playWhenReady = currentPage == page
            playerPage.seekToDefaultPosition(playerConfig.second)
            playerPage
        } else {
            null
        }
        PlayerSurface(
            modifier = Modifier.fillMaxHeight(),
            player = player,
            scaleMode = ScaleMode.Zoom
        )
        Text(text = "Page $page")
    }
}
