/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * A view model that exposes some timed events.
 *
 * @param application The [Application].
 */
class TimeBasedContentViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Player
     */
    val player = PillarboxExoPlayer(application)

    /**
     * Timed events
     */
    val deltaTimeEvents: StateFlow<List<DeltaTimeEvent>> = flow {
        emit(
            listOf(
                DeltaTimeEvent(name = "Now", Duration.ZERO),
                DeltaTimeEvent(name = "2 hour in the past", (-2).hours),
                DeltaTimeEvent(name = "1 hour in the past", (-1).hours),
                DeltaTimeEvent(name = "Near future", 30.seconds),
                DeltaTimeEvent(name = "In 1 hour", 1.hours),
                DeltaTimeEvent(name = "4 hour in the past", (-4).hours),
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        player.setMediaItem(DemoItem.LiveTimestampVideoHLS.toMediaItem())
        player.prepare()
    }

    override fun onCleared() {
        player.release()
    }

    /**
     * @property name Name of the event.
     * @property delta The delta [Duration] of the event from now.
     */
    data class DeltaTimeEvent(
        val name: String,
        val delta: Duration,
    )
}
