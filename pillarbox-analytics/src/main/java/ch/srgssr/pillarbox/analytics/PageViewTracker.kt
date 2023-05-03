/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Page view tracker send page views only if the last page view is different.
 *
 * @property pageViewAnalytics The [PageViewAnalytics] implementation to send page views.
 */
class PageViewTracker(private val pageViewAnalytics: PageViewAnalytics) : PageViewAnalytics {
    private var lastPageView: PageView? = null

    override fun sendPageView(pageView: PageView) {
        if (lastPageView != pageView) {
            pageViewAnalytics.sendPageView(pageView)
            lastPageView = pageView
        }
    }

    /**
     * Clear [lastPageView]
     */
    fun clear() {
        lastPageView = null
    }
}
