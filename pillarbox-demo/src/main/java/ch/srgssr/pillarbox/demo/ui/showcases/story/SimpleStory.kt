/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * A sample trying to reproduce story like TikTok.
 * Each page own its PillarboxPlayer and release it when no more needed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleStory() {
    val context = LocalContext.current
    val playlist = remember {
        Playlist.VideoUrls
    }

    val pagerState = rememberPagerState() { playlist.items.size }
    HorizontalPager(
        modifier = Modifier.fillMaxHeight(),
        key = { page -> playlist.items[page].uri },
        state = pagerState
    ) { page ->
        SimpleStoryPlayer(demoItem = playlist.items[page], isPlaying = pagerState.currentPage == page)
    }
}

/**
 * Simple story player
 * Each DemoItem have a [PillarboxPlayer], the player is released onDispose
 *
 * @param demoItem The DemoItem to play
 * @param isPlaying to pause or play the player
 */
@Composable
private fun SimpleStoryPlayer(demoItem: DemoItem, isPlaying: Boolean = false) {
    val context = LocalContext.current
    val player = remember {
        PillarboxPlayer(
            context = context,
            mediaItemSource = PlayerModule.provideMixedItemSource(context),
            loadControl = StoryLoadControl.build()
        ).apply {
            setMediaItem(demoItem.toMediaItem())
            prepare()
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    player.playWhenReady = isPlaying

    DisposableEffect(
        AndroidView(factory = {
            PlayerView(context).apply {
                hideController()
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                this.player = player
            }
        })
    ) {
        onDispose {
            player.release()
        }
    }
}
