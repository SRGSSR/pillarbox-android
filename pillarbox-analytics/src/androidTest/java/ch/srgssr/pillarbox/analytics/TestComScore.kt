/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestComScore {

    private lateinit var comScore: ComScore

    @Before
    fun setup() {
        val appContext = getInstrumentation().targetContext

        val config = ComScore.Config()
        comScore = ComScore.init(config = TestUtils.analyticsConfig, config, appContext)
    }

    @Test(expected = AssertionError::class)
    fun testSendEvent() {
        comScore.sendEvent(Event("Event1"))
    }

    @Test
    fun testSendPageView() {
        comScore.sendPageViewEvent(PageEvent("PageTitle1", arrayOf("pillarbox", "unit-test")))
        Assert.assertTrue(true)
    }
}
