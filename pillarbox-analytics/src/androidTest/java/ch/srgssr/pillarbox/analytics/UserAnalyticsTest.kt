/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import org.junit.Assert
import org.junit.Test

class UserAnalyticsTest {

    private val config = SRGAnalytics.Config(
        analyticsConfig = AnalyticsConfig(distributor = AnalyticsConfig.BuDistributor.SRG),
        commandersAct = CommandersAct.Config(virtualSite = "pillarbox-test-android", sourceKey = CommandersAct.Config.SOURCE_KEY_SRG_DEBUG)
    )

    @Test
    fun testUserId() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        SRGAnalytics.userId = "Toto"
        SRGAnalytics.isLogged = false
        val analytics = SRGAnalytics.init(appContext = appContext, config = config)

        Assert.assertEquals(analytics.userId, analytics.commandersAct.userId)
        Assert.assertEquals(analytics.isLogged, analytics.commandersAct.isLogged)

        analytics.userId = "userId"
        analytics.isLogged = true

        Assert.assertEquals(analytics.userId, analytics.commandersAct.userId)
        Assert.assertEquals(analytics.isLogged, analytics.commandersAct.isLogged)
    }

}
