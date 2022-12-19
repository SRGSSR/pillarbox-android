/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations.story

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.ui.ExoplayerView
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
        state = pagerState
    ) { page ->
        val playerConfig = storyViewModel.getPlayerAndMediaItemIndexForPage(page)
        val player = storyViewModel.getPlayerFromIndex(playerConfig.first)
        storyViewModel.seekTo(playerConfig)
        player.playWhenReady = currentPage == page
        ExoplayerView(
            player = if (page == currentPage - 1 || page == currentPage + 1 || page == currentPage) player else null,
            showBuffering = PlayerView.SHOW_BUFFERING_ALWAYS,
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
            showPreviousButton = false,
            showNextButton = false,
            useController = true,
            controllerAutoShow = false,
            shutterBackgroundColor = Color.TRANSPARENT
        )
    }
}
