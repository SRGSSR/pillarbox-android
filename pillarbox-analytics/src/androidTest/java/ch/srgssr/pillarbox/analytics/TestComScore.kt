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
        comScore = ComScore.init(config = TestUtils.analyticsConfig, appContext = appContext)
    }

    @Test
    fun testSendPageView() {
        comScore.sendPageView(PageView("PageTitle1", arrayOf("pillarbox", "unit-test")))
        Assert.assertTrue(true)
    }
}
