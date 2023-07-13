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
        vendor = AnalyticsConfig.Vendor.SRG,
        virtualSite = "pillarbox-test-android",
        sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
    )

    @Test
    fun testInitTwice() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val analytics = SRGAnalytics(context = appContext, config = config)
        Assert.assertEquals(analytics.comScore, SRGAnalytics(appContext, config).comScore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInitTwiceDifferentConfig() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        SRGAnalytics(context = appContext, config = config)
        val config2 = config.copy(vendor = AnalyticsConfig.Vendor.RSI, "pillarbox-test-fail")
        SRGAnalytics(appContext, config2)
    }
}
