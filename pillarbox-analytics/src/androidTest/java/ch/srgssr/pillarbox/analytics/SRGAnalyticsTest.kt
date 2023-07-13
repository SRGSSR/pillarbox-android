/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test

class SRGAnalyticsTest {

    private val config = AnalyticsConfig(
        vendor = AnalyticsConfig.Vendor.SRG,
        virtualSite = "pillarbox-test-android",
        sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
    )

    @Test(expected = IllegalArgumentException::class)
    fun testInitTwice() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        SRGAnalytics.init(appContext as Application, config)
        SRGAnalytics.init(appContext, config.copy(vendor = AnalyticsConfig.Vendor.RSI))
    }

}
