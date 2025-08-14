/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.extension.getCreditAtPosition

/**
 * Remembers the value of a [CreditState] created based on the passed [Player] and launches a
 * coroutine to listen to the [Player's][Player] changes. If the [Player] instance changes between
 * compositions, this produces and remembers a new [CreditState].
 *
 * **Sample usage:**
 *
 * ```kotlin
 * val creditState = rememberCreditState(player)
 *
 * if (creditState.isInCredit) {
 *     Button(onClick = creditState::onClick) {
 *         Text(text = "Skip")
 *     }
 * }
 * ```
 */
@Composable
fun rememberCreditState(player: Player): CreditState {
    val creditState = remember(player) {
        CreditState(player)
    }

    DisposableEffect(player) {
        creditState.startObserving()

        onDispose {
            creditState.stopObserving()
        }
    }

    return creditState
}

/**
 * State that holds information to correctly deal with UI components related to the current [Credit].
 *
 * @param player The [Player] instance.
 */
class CreditState(private val player: Player) {
    private val creditListener = object : PillarboxPlayer.Listener {
        override fun onCreditChanged(credit: Credit?) {
            currentCredit = credit
        }
    }

    /**
     * Whether the current [player position][Player.getCurrentPosition] is inside a [Credit].
     */
    val isInCredit by derivedStateOf { currentCredit != null }

    /**
     * The current [Credit].
     */
    var currentCredit: Credit? by mutableStateOf(player.getCreditAtPosition())
        private set

    /**
     * Handles the interaction with a "Skip credit" button by seeking to the end of the current [Credit]. This does nothing if [currentCredit] is
     * `null`.
     */
    fun onClick() {
        currentCredit?.let { credit ->
            player.seekTo(credit.end)
        }
    }

    /**
     * Starts observing the player for [Credit] changes.
     */
    fun startObserving() {
        player.addListener(creditListener)
    }

    /**
     * Stops observing the player for [Credit] changes.
     */
    fun stopObserving() {
        player.removeListener(creditListener)
    }
}
