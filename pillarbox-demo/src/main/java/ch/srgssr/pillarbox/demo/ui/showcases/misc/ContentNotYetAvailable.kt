/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.PlaybackException
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.demo.ui.player.Countdown
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * Content not yet available
 */
@Composable
fun ContentNotYetAvailable() {
    val viewModel: ContentNotYetAvailableViewModel = viewModel()
    val player = viewModel.player
    PlayerSurface(player = player) {
        val error by player.playerErrorAsState()
        error?.let {
            ErrorViewWithCountdown(
                error = it,
                modifier = Modifier.fillMaxSize(),
                onCountdownEnd = {
                    player.prepare()
                    player.play()
                },
                onRetry = player::prepare
            )
        }
    }
}

@Composable
private fun ErrorViewWithCountdown(
    error: PlaybackException,
    modifier: Modifier = Modifier,
    onCountdownEnd: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val cause = error.cause
    when {
        cause is BlockReasonException.StartDate && cause.instant != null -> {
            val duration = cause.instant!!.minus(Clock.System.now())
            CountdownView(duration, Modifier.fillMaxSize(), onCountdownEnd)
        }

        else -> {
            PlayerError(playerError = error, modifier = modifier, onRetry = onRetry)
        }
    }
}

@Composable
private fun CountdownView(duration: Duration, modifier: Modifier = Modifier, onCountdownEnd: () -> Unit = {}) {
    Box(
        modifier = modifier,
    ) {
        LaunchedEffect(Unit) {
            delay(duration)
            onCountdownEnd()
        }
        Countdown(
            modifier = Modifier.align(Alignment.Center),
            countdownDuration = duration
        )
    }
}
