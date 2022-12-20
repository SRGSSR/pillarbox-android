/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations.story

import androidx.compose.foundation.layout.fillMaxHeight
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
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.DemoPlaylistProvider
import ch.srgssr.pillarbox.demo.data.Dependencies
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**
 * A sample trying to reproduce story like TikTok.
 * Each page own its PillarboxPlayer and release it when no more needed.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun SimpleStory() {
    val context = LocalContext.current
    val playlist = remember {
        DemoPlaylistProvider(context = context).loadDemoItemFromAssets("playlists.json")[0]
    }

    val pagerState = rememberPagerState()
    HorizontalPager(
        modifier = Modifier.fillMaxHeight(),
        key = { page -> playlist.items[page].uri },
        count = playlist.items.size,
        state = pagerState
    ) { page ->
        SimpleStoryPlayer(demoItem = playlist.items[page], isPlaying = currentPage == page)
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
            mediaItemSource = Dependencies.provideMixedItemSource(context),
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
