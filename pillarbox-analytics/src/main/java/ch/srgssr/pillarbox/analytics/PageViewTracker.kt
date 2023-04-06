/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration

/**
 * Page view tracker
 */
class PageViewTracker(private val analytics: SRGAnalytics) {

    private var lastPageEvent: PageEvent? = null

    private val applicationCallback = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
            // Nothing
        }

        override fun onLowMemory() {
            // Nothing
        }

        override fun onTrimMemory(level: Int) {
            clear()
        }
    }

    /**
     * Track page view
     *
     * @param pageEvent
     */
    fun trackPageView(pageEvent: PageEvent) {
        if (lastPageEvent != pageEvent) {
            lastPageEvent = pageEvent
            analytics.sendPageViewEvent(pageEvent)
        }
    }

    private fun clear() {
        lastPageEvent = null
    }

    /**
     * Start tracking of the page view
     *
     * PageEvent are automatically send if a Activity or a Fragment is visible and implement [PageTrackable].
     *
     * @param application Application
     */
    fun start(application: Application) {
        application.registerComponentCallbacks(applicationCallback)
    }

    /**
     * Stop tracking of the page view
     *
     * @param application Application
     */
    fun stop(application: Application) {
        application.unregisterComponentCallbacks(applicationCallback)
    }
}
