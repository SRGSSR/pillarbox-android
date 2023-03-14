/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Media item tracker
 */
interface MediaItemTracker {

    /**
     * Start Media tracking.
     *
     * @param player The player to track.
     */
    fun start(player: PillarboxPlayer)

    /**
     * Stop Media tracking.
     *
     * @param player The player tracked.
     */
    fun stop(player: PillarboxPlayer)
}
