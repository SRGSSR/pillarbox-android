/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.analytics.comscore.ComScoreSrg
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestComScoreSrg {

    private lateinit var comScore: ComScoreSrg

    @Before
    fun setup() {
        val appContext = getInstrumentation().targetContext.applicationContext
        comScore = ComScoreSrg.init(config = TestUtils.analyticsConfig, context = appContext)
    }

    @Test
    fun testSendPageView() {
        comScore.sendPageView("PageTitle1")
        Assert.assertTrue(true)
    }
}
