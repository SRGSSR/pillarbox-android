/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository

/**
 * Default media item tracker repository for SRG.
 *
 * @property trackerRepository The MediaItemTrackerRepository to use to store Tracker.Factory.
 * @param commandersAct
 */
class DefaultMediaItemTrackerRepository internal constructor(
    private val trackerRepository: MediaItemTrackerRepository,
    commandersAct: CommandersAct = SRGAnalytics.commandersAct
) :
    MediaItemTrackerProvider by
    trackerRepository {
    init {
        registerFactory(SRGEventLoggerTracker::class.java, SRGEventLoggerTracker.Factory())
        registerFactory(ComScoreTracker::class.java, ComScoreTracker.Factory())
        registerFactory(CommandersActTracker::class.java, CommandersActTracker.Factory(commandersAct))
    }

    constructor() : this(trackerRepository = MediaItemTrackerRepository())

    /**
     * Register factory
     * @see MediaItemTrackerRepository.registerFactory
     * @param T Class type extends [MediaItemTracker]
     * @param trackerClass The class the trackerFactory create. Clazz must extends MediaItemTracker.
     * @param trackerFactory The tracker factory associated with clazz.
     */
    fun <T : MediaItemTracker> registerFactory(trackerClass: Class<T>, trackerFactory: MediaItemTracker.Factory) {
        trackerRepository.registerFactory(trackerClass, trackerFactory)
    }
}
