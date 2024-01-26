/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import io.mockk.mockk
import io.mockk.verifySequence
import kotlin.test.Test

class DefaultMediaItemTrackerRepositoryTest {
    @Test
    fun `DefaultMediaItemTrackerRepository registers some default factories`() {
        val trackerRepository = mockk<MediaItemTrackerRepository>(relaxed = true)
        val commandersAct = mockk<CommandersAct>()

        DefaultMediaItemTrackerRepository(
            trackerRepository = trackerRepository,
            commandersAct = commandersAct,
        )

        verifySequence {
            trackerRepository.registerFactory(SRGEventLoggerTracker::class.java, any(SRGEventLoggerTracker.Factory::class))
            trackerRepository.registerFactory(ComScoreTracker::class.java, any(ComScoreTracker.Factory::class))
            trackerRepository.registerFactory(CommandersActTracker::class.java, any(CommandersActTracker.Factory::class))
        }
    }
    @Test
    fun `DefaultMediaItemTrackerRepository registers some default factories without CommandersAct`() {
        val trackerRepository = mockk<MediaItemTrackerRepository>(relaxed = true)

        DefaultMediaItemTrackerRepository(
            trackerRepository = trackerRepository,
            commandersAct = null,
        )

        verifySequence {
            trackerRepository.registerFactory(SRGEventLoggerTracker::class.java, any(SRGEventLoggerTracker.Factory::class))
            trackerRepository.registerFactory(ComScoreTracker::class.java, any(ComScoreTracker.Factory::class))
            trackerRepository.registerFactory(CommandersActTracker::class.java, any(CommandersActTracker.Factory::class))
        }
    }
}
