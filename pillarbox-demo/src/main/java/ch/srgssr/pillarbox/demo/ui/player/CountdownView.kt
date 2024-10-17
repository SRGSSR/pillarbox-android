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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
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
    private var countdown by mutableStateOf(duration)

    /**
     * Remaining time [LocalTime].
     */
    val remainingTime: State<LocalTime> = derivedStateOf {
        LocalTime.fromMillisecondOfDay(countdown.inWholeMilliseconds.toInt())
    }

    internal suspend fun start() {
        while (countdown > ZERO) {
            delay(step)
            countdown -= step
        }
    }

    private companion object {
        val step = 1.seconds
    }
}

private val formatHms by lazy {
    LocalTime.Format {
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
        char(':')
        second(Padding.ZERO)
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
    val remainingTime by countdownState.remainingTime
    val text = remainingTime.format(formatHms)
    Text(text, modifier = modifier, color = Color.White)
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
                countdownDuration = 1.minutes,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
