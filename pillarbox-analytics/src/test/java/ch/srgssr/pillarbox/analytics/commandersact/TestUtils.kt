/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SourceKey

object TestUtils {
    val analyticsConfig = AnalyticsConfig(
        vendor = AnalyticsConfig.Vendor.SRG,
        appSiteName = "pillarbox-test-android",
        sourceKey = SourceKey.SRG_DEBUG
    )
}
