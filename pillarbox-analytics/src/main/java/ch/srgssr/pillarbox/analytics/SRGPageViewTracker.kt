/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * SRG Page view tracker that send page view to [SRGAnalytics].
 */
object SRGPageViewTracker : PageViewAnalytics {
    private var pageViewTracker: PageViewTracker = PageViewTracker(SRGAnalytics)

    override fun sendPageView(pageView: PageView) {
        pageViewTracker.sendPageView(pageView)
    }
}
