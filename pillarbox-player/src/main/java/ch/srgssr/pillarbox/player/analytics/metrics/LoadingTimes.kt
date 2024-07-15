/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class LoadingTimes(
    private val onLoadingReady: OnLoadingRead,
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    var source: Duration? = null,
    var manifest: Duration? = null,
    var asset: Duration? = null,
    var drm: Duration? = null,
) {

    fun interface OnLoadingRead {
        fun onReady()
    }

    private var bufferingStartTime: Long = 0L
    var timeToReady: Duration? = null
        private set

    var state: @Player.State Int = Player.STATE_IDLE
        set(value) {
            if (field == value) return
            if (field == Player.STATE_READY && value == Player.STATE_BUFFERING) return

            if ((field == Player.STATE_IDLE || field == Player.STATE_ENDED) && value == Player.STATE_BUFFERING) {
                bufferingStartTime = timeProvider()
            }
            if (field == Player.STATE_BUFFERING && value == Player.STATE_READY) {
                timeToReady = (timeProvider() - bufferingStartTime).milliseconds
            }
            field = value
            if (field == Player.STATE_READY) {
                onLoadingReady.onReady()
            }
        }

    override fun toString(): String {
        return "LoadingTimes(bufferingStartTime=$bufferingStartTime, timeToReady=$timeToReady, state=${StringUtil.playerStateString(state)})"
    }
}
