/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Page view tracker which only sends a page view when the previous page view is different.
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
     * @return the last page view if any.
     */
    fun clear(): PageView? {
        val output = lastPageView
        lastPageView = null
        return output
    }
}
