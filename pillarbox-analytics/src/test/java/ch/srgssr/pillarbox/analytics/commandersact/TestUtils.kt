/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.AnalyticsConfig

object TestUtils {
    val analyticsConfig = AnalyticsConfig(
        vendor = AnalyticsConfig.Vendor.SRG,
        appSiteName = "pillarbox-test-android",
        sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
    )
}
