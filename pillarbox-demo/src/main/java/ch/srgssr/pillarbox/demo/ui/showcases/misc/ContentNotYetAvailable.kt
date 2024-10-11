/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.player.Countdown
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes

/**
 * Content not yet available
 */
@Composable
fun ContentNotYetAvailable() {
    val viewModel: CountDownViewModel = viewModel()
    val player = viewModel.player
    PlayerSurface(player = player) {
        val error by player.playerErrorAsState()
        error?.let {
            ErrorViewWithCountDow(
                error = it,
                modifier = Modifier
                    .fillMaxSize(),
                onCountDownEnd = {
                    player.prepare()
                    player.play()
                },
                onRetry = player::prepare
            )
        }
    }
}

@Composable
private fun ErrorViewWithCountDow(
    error: PlaybackException,
    modifier: Modifier = Modifier,
    onCountDownEnd: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    when (val cause = error.cause) {
        is BlockReasonException -> {
            if (cause.blockReason == BlockReason.STARTDATE && cause.validFrom != null) {
                val duration = cause.validFrom!!.minus(Clock.System.now())
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LaunchedEffect(Unit) {
                        delay(duration)
                        onCountDownEnd()
                    }
                    Countdown(
                        modifier = Modifier.align(Alignment.Center),
                        countDownDuration = duration
                    )
                }
            } else {
                PlayerError(playerError = error, modifier = modifier, onRetry = onRetry)
            }
        }

        else -> {
            PlayerError(playerError = error, modifier = modifier, onRetry = onRetry)
        }
    }
}

internal class CountDownViewModel(application: Application) : AndroidViewModel(application) {
    private class AlwaysStartDateBlockedAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        private val srgAssetLoader = SRGAssetLoader(context)
        private val validFrom = Clock.System.now().plus(1.minutes)
        override fun canLoadAsset(mediaItem: MediaItem): Boolean {
            return srgAssetLoader.canLoadAsset(mediaItem)
        }

        override suspend fun loadAsset(mediaItem: MediaItem): Asset {
            if (validFrom.minus(Clock.System.now()) <= ZERO) {
                return srgAssetLoader.loadAsset(mediaItem)
            }
            throw BlockReasonException(BlockReason.STARTDATE, validFrom = validFrom)
        }
    }

    val player: PillarboxExoPlayer = PillarboxExoPlayer(
        context = application,
        mediaSourceFactory = PillarboxMediaSourceFactory(
            context =
            application
        ).apply {
            addAssetLoader(AlwaysStartDateBlockedAssetLoader(application))
        }
    )

    init {
        player.prepare()
        player.setMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
        player.play()
    }

    override fun onCleared() {
        player.release()
    }
}
