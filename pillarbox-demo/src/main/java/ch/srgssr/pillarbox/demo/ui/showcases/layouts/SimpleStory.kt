/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface

/**
 * A sample trying to reproduce story like TikTok.
 * Each page own its PillarboxPlayer and release it when no more needed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleStory() {
    val playlist = remember {
        Playlist.VideoUrns
    }

    val pagerState = rememberPagerState { playlist.items.size }
    HorizontalPager(
        modifier = Modifier.fillMaxHeight(),
        key = { page -> playlist.items[page].uri },
        beyondBoundsPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(0),
            snapAnimationSpec = spring(stiffness = Spring.StiffnessHigh)
        ),
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
    val player = remember(demoItem) {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItem(demoItem.toMediaItem())
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    DisposableEffect(player) {
        player.prepare()
        onDispose {
            player.release()
        }
    }
    PlayerSurface(
        modifier = Modifier.fillMaxSize(),
        player = player,
        scaleMode = ScaleMode.Crop
    )

    LifecycleStartEffect(isPlaying) {
        player.playWhenReady = isPlaying
        onStopOrDispose {
            player.pause()
        }
    }
}
