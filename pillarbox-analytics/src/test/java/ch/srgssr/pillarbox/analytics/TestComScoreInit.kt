/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.comscore.ComScore
import org.junit.Test

class TestComScoreInit {

    @Test
    fun testComScoreInit() {
        val analyticsConfig = AnalyticsConfig(distributor = AnalyticsConfig.BuDistributor.SRG, "site")
        val config = ComScore.Config()
        //ComScore.init(config = analyticsConfig,config,mockk())
    }
}
