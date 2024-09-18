/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.util.SparseArray

/**
 * Pool of [playersCount] [PillarboxExoPlayer].
 *
 * @param playersCount The maximum number of [PillarboxExoPlayer] managed by this pool.
 * @param playerFactory The factory method to create a new [PillarboxExoPlayer].
 */
class PlayerPool(
    private val playersCount: Int,
    private val playerFactory: () -> PillarboxExoPlayer,
) {
    private val players: SparseArray<PillarboxExoPlayer>

    init {
        require(playersCount > 0) {
            "playersCount must be greater than 0, but was $playersCount"
        }

        players = SparseArray<PillarboxExoPlayer>(playersCount)
    }

    /**
     * Get a [PillarboxExoPlayer] for the given [position]. If the desired player has not been created yet, [playerFactory] will be called.
     *
     * @param position The position of the [PillarboxExoPlayer] to retrieve.
     * @return The desired [PillarboxExoPlayer], or `null` if [position] is negative.
     */
    fun getPlayerAtPosition(position: Int): PillarboxExoPlayer? {
        if (position < 0) {
            return null
        }

        val index = position % playersCount

        return players[index] ?: playerFactory().also {
            players[index] = it
        }
    }

    /**
     * Release this pool. This will also release all the managed [PillarboxExoPlayer].
     */
    fun release() {
        repeat(playersCount) { index ->
            players[index]?.release()
        }
        players.clear()
    }
}
