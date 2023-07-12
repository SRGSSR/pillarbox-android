/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test

class SRGAnalyticsTest {

    private val config = AnalyticsConfig(
        distributor = AnalyticsConfig.BuDistributor.SRG,
        virtualSite = "pillarbox-test-android",
        sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
    )

    @Test
    fun testInitTwice() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val analytics = SRGAnalytics.init(appContext = appContext, config = config)
        Assert.assertEquals(analytics, SRGAnalytics.init(appContext, config))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInitTwiceDifferentConfig() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        SRGAnalytics.init(appContext = appContext, config = config)
        val config2 = config.copy(distributor = AnalyticsConfig.BuDistributor.RSI, "pillarbox-test-fail")
        SRGAnalytics.init(appContext, config2)
    }
}
