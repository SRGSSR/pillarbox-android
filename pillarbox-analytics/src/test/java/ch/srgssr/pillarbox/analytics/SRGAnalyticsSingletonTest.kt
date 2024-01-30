/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.comscore.Analytics
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class SRGAnalyticsSingletonTest {

    private val config = AnalyticsConfig(
        vendor = AnalyticsConfig.Vendor.SRG,
        appSiteName = "pillarbox-test-android",
        sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
    )

    @BeforeTest
    fun setup() {
        mockkStatic(Analytics::class)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInitTwice() {
        val appContext: Context = ApplicationProvider.getApplicationContext()
        SRGAnalytics.init(appContext as Application, config)
        SRGAnalytics.init(appContext, config.copy(vendor = AnalyticsConfig.Vendor.RSI))
    }
}
