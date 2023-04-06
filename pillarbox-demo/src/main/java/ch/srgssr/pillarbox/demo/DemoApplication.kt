/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Activity
import android.app.Application
import ch.srgssr.pillarbox.analytics.PageEvent
import ch.srgssr.pillarbox.analytics.PageViewTracker
import ch.srgssr.pillarbox.demo.di.AnalyticsModule

/**
 * Demo application
 * - Handle SRGAnalytics initialization and page view tracker
 */
class DemoApplication : Application() {

    /**
     * Page view tracker
     */
    lateinit var pageTracker: PageViewTracker

    override fun onCreate() {
        super.onCreate()
        pageTracker = PageViewTracker(AnalyticsModule.providerAnalytics(this))
        pageTracker.start(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        pageTracker.stop(this)
    }
}

/**
 * Track page view
 *
 * @param pageEvent
 */
fun Activity.trackPageView(pageEvent: PageEvent) {
    val pageTracker = (application as DemoApplication).pageTracker
    pageTracker.trackPageView(pageEvent)
}
