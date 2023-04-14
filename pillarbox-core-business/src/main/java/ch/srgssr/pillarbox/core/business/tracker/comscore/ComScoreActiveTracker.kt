/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.comscore

import com.comscore.Analytics
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ComScore UxActive trackers to handle multiple ComScoreTracker / player that concurrently play/pause.
 */
internal object ComScoreActiveTracker {
    private val activeTrackers = ConcurrentHashMap<ComScoreTracker, Boolean>()
    private val isActive = AtomicBoolean(false)

    /**
     * Get current active state
     */
    fun getIsActive(): Boolean = isActive.get()

    /**
     * Get current active trackers
     */
    fun getActiveTrackers(): Map<ComScoreTracker, Boolean> = activeTrackers.toMap()

    /**
     * Notify ux active
     */
    fun notifyUxActive(tracker: ComScoreTracker) {
        activeTrackers[tracker] = true
        if (!isActive.getAndSet(true)) {
            Analytics.notifyUxActive()
        }
    }

    /**
     * Notify ux active
     */
    fun notifyUxInactive(tracker: ComScoreTracker) {
        activeTrackers[tracker] = false
        activeTrackers.remove(tracker)
        if (activeTrackers.values.none { it } && isActive.getAndSet(false)) {
            Analytics.notifyUxInactive()
        }
    }
}
