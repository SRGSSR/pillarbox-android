/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.story

import android.util.Pair
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import ch.srg.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srg.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import retrofit2.HttpException

/**
 * Story home
 *
 * A sample trying to reproduce story like TikTok.
 *
 * Surface view may sometimes keep on screen. Maybe if we use TextView with PlayerView this strange behavior will disappear.
 * The main thing is to remove player from a PlayerView when no more needed. Don't know for now how to do that with Compose.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun StoryHome(storyViewModel: StoryViewModel = viewModel()) {
    val pagerState = rememberPagerState()
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(pagerState) {
        // Collect from the pager state a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { page ->
            storyViewModel.setCurrentPage(page)
        }
    }

    HorizontalPager(modifier = Modifier.fillMaxHeight(), count = storyViewModel.playlist.items.size, state = pagerState) { page ->
        val previousPage = currentPage - 1
        val nextPage = currentPage + 1
        if (page in previousPage..nextPage) {
            val player = storyViewModel.getPlayerForPageNumber(page)
            StoryPlayer(player = player)
            if (page == currentPage) {
                player.play()
            } else {
                player.pause()
            }
        } else {
            StoryPlayer(player = null)
        }
    }
}

@Composable
private fun StoryPlayer(player: PillarboxPlayer?) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).also { view ->
                view.controllerAutoShow = false
                view.useController = true
                view.setShowNextButton(false)
                view.setShowPreviousButton(false)
                view.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                view.setErrorMessageProvider { throwable ->
                    when (val cause = throwable.cause) {
                        is BlockReasonException -> {
                            Pair.create(0, cause.blockReason)
                        }
                        is HttpException -> {
                            Pair.create(cause.code(), cause.message)
                        }
                        is ResourceNotFoundException -> {
                            Pair.create(0, "Can't find Resource to play")
                        }
                        else -> {
                            Pair.create(throwable.errorCode, "${throwable.localizedMessage} (${throwable.errorCodeName})")
                        }
                    }
                }
            }
        },
        update = { view ->
            view.player = player
        }
    )
}
