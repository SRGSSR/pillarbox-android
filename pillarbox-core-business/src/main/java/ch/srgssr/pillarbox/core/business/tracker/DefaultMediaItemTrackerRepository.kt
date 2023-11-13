/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository

/**
 * Default media item tracker repository for SRG.
 *
 * @property trackerRepository The MediaItemTrackerRepository to use to store Tracker.Factory.
 * @param commandersAct CommanderAct instance to use for tracking. If set to null no tracking is made.
 */
class DefaultMediaItemTrackerRepository internal constructor(
    private val trackerRepository: MediaItemTrackerRepository,
    commandersAct: CommandersAct?
) :
    MediaItemTrackerProvider by
    trackerRepository {
    init {
        registerFactory(SRGEventLoggerTracker::class.java, SRGEventLoggerTracker.Factory())
        registerFactory(ComScoreTracker::class.java, ComScoreTracker.Factory())
        val commanderActOrEmpty = commandersAct ?: EmptyCommandersAct
        registerFactory(CommandersActTracker::class.java, CommandersActTracker.Factory(commanderActOrEmpty))
    }

    constructor() : this(trackerRepository = MediaItemTrackerRepository(), SRGAnalytics.commandersAct)

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

    private object EmptyCommandersAct : CommandersAct {
        override fun sendPageView(pageView: CommandersActPageView) {
            // Nothing
        }

        override fun sendEvent(event: CommandersActEvent) {
            // Nothing
        }

        override fun sendTcMediaEvent(event: TCMediaEvent) {
            // Nothing
        }

        override fun putPermanentData(labels: Map<String, String>) {
            // Nothing
        }

        override fun removePermanentData(label: String) {
            // Nothing
        }

        override fun getPermanentDataLabel(label: String): String? {
            return null
        }

        override fun setConsentServices(consentServices: List<String>) {
            // Nothing
        }
    }
}
