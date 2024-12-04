/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * @param duration Countdown duration.
 * @return [CountdownState] what starts the countdown.
 */
@Composable
fun rememberCountdownState(duration: Duration): CountdownState {
    val state = remember(duration) {
        CountdownState(duration)
    }
    LaunchedEffect(state) {
        state.start()
    }
    return state
}

/**
 * Count down state
 *
 * @param duration The countdown duration.
 */
class CountdownState internal constructor(duration: Duration) {
    private var _countdown = mutableStateOf(duration.inWholeSeconds.seconds)

    /**
     * Remaining time [LocalTime].
     */
    val countdown: State<Duration> = _countdown

    internal suspend fun start() {
        while (_countdown.value > ZERO) {
            delay(step)
            _countdown.value -= step
        }
    }

    private companion object {
        val step = 1.seconds
    }
}

/**
 * Countdown
 *
 * @param countdownDuration The amount of time until the countdown ends.
 * @param modifier The [Modifier] to layouts this view.
 */
@Composable
fun Countdown(countdownDuration: Duration, modifier: Modifier = Modifier) {
    val countdownState = rememberCountdownState(countdownDuration)
    val remainingTime by countdownState.countdown
    Text("$remainingTime", modifier = modifier, color = Color.White)
}

@Preview(showBackground = true)
@Composable
private fun CountdownPreview() {
    PillarboxTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Countdown(
                countdownDuration = 40.hours + 1.minutes + 32.seconds,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
