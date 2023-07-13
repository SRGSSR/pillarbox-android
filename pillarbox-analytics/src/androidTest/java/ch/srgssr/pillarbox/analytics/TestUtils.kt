/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

object TestUtils {
    val analyticsConfig = AnalyticsConfig(
        vendor = AnalyticsConfig.Vendor.SRG,
        virtualSite = "pillarbox-test-android",
        sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
    )
}
