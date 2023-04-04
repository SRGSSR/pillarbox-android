/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.tracker.ComScoreActiveTracker
import ch.srgssr.pillarbox.core.business.tracker.ComScoreTracker
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class ComScoreActiveTrackerTest {

    companion object {
        val empty = emptyMap<ComScoreTracker, Boolean>()
    }

    @Test
    fun testOneTracker() {
        Assert.assertEquals(empty, ComScoreActiveTracker.getActiveTrackers())
        val tracker: ComScoreTracker = mockk(relaxed = false)
        ComScoreActiveTracker.notifyUxActive(tracker)
        Assert.assertTrue(ComScoreActiveTracker.getIsActive())
        Assert.assertEquals(mapOf(Pair(tracker, true)), ComScoreActiveTracker.getActiveTrackers())

        ComScoreActiveTracker.notifyUxInactive(tracker)
        Assert.assertEquals(empty, ComScoreActiveTracker.getActiveTrackers())
        Assert.assertFalse(ComScoreActiveTracker.getIsActive())
    }

    @Test
    fun testOneTrackerSecondTime() {
        Assert.assertEquals(empty, ComScoreActiveTracker.getActiveTrackers())

        val tracker: ComScoreTracker = mockk(relaxed = false)
        ComScoreActiveTracker.notifyUxActive(tracker)
        Assert.assertTrue(ComScoreActiveTracker.getIsActive())

        ComScoreActiveTracker.notifyUxInactive(tracker)
        Assert.assertFalse(ComScoreActiveTracker.getIsActive())
    }

    @Test
    fun testMultipleTracker() {
        Assert.assertEquals(empty, ComScoreActiveTracker.getActiveTrackers())

        val tracker: ComScoreTracker = mockk(relaxed = false)
        val tracker2: ComScoreTracker = mockk(relaxed = false)
        ComScoreActiveTracker.notifyUxActive(tracker)
        Assert.assertTrue(ComScoreActiveTracker.getIsActive())
        ComScoreActiveTracker.notifyUxActive(tracker2)
        Assert.assertTrue(ComScoreActiveTracker.getIsActive())
        Assert.assertEquals(mapOf(Pair(tracker, true), Pair(tracker2, true)), ComScoreActiveTracker.getActiveTrackers())


        ComScoreActiveTracker.notifyUxInactive(tracker)
        Assert.assertTrue(ComScoreActiveTracker.getIsActive())
        Assert.assertEquals(mapOf(Pair(tracker2, true)), ComScoreActiveTracker.getActiveTrackers())

        ComScoreActiveTracker.notifyUxInactive(tracker2)
        Assert.assertFalse(ComScoreActiveTracker.getIsActive())
        Assert.assertEquals(empty, ComScoreActiveTracker.getActiveTrackers())

    }
}
