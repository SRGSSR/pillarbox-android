/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestComScoreInit {

    private lateinit var comScore: ComScore

    @Before
    fun setup() {
        val appContext = getInstrumentation().targetContext
        val analyticsConfig = AnalyticsConfig(distributor = AnalyticsConfig.BuDistributor.SRG, "site")
        val config = ComScore.Config()
        comScore = ComScore.init(config = analyticsConfig, config, appContext)
    }

    @After
    fun tearDown() {
        // Nothing
    }

    @Test(expected = AssertionError::class)
    fun testSendEvent() {
        comScore.sendEvent(Event("Event1"))
    }

    @Test
    fun testComScoreInit() {
        comScore.sendPageViewEvent(PageEvent("PageTitle1", arrayOf("pillarbox", "unit-test")))
        Assert.assertTrue(true)
    }
}
