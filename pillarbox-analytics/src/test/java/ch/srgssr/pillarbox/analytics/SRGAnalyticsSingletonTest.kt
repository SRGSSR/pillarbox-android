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
import io.mockk.clearAllMocks
import io.mockk.mockkStatic
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
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

    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        shadowOf(context.packageManager).getInternalMutablePackageInfo(context.packageName).versionName = "1.2.3"

        mockkStatic(Analytics::class)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInitTwice() {
        val application = context as Application
        SRGAnalytics.init(application, config)
        SRGAnalytics.init(application, config.copy(vendor = AnalyticsConfig.Vendor.RSI))
    }
}
