/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import androidx.core.app.ComponentActivity
import androidx.lifecycle.LiveData

/**
 * SRG Page view tracker that match SRG page view specifications.
 *  - When come back from background send page view again
 *  - Don't send same page view twice
 *
 * Call start inside your Application.onCreate and make sur SRGAnalytics was init!
 */
object SRGPageViewTracker : PageViewAnalytics {
    private var pageViewTracker: PageViewTracker? = null

    /**
     * Trackable
     */
    interface Trackable {
        /**
         * Page view to send when it is ready.
         */
        val pageView: LiveData<PageView?>
    }

    private val applicationCallback = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
            // Nothing
        }

        override fun onLowMemory() {
            // Nothing
        }

        override fun onTrimMemory(level: Int) {
            pageViewTracker?.clear()
        }
    }

    private val activityLifecycleCallBacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // Nothing
        }

        override fun onActivityStarted(activity: Activity) {
            // Nothing
        }

        override fun onActivityResumed(activity: Activity) {
            // Nothing
        }

        override fun onActivityPostResumed(activity: Activity) {
            if (activity is Trackable && activity is ComponentActivity) {
                activity.pageView.observe(activity) {
                    it?.let { pageView -> sendPageView(pageView) }
                }
            }
        }

        override fun onActivityPaused(activity: Activity) {
            // Nothing
        }

        override fun onActivityStopped(activity: Activity) {
            // Nothing
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // Nothing
        }

        override fun onActivityDestroyed(activity: Activity) {
            // Nothing
        }
    }

    /**
     * Start tracking of the page view
     *
     * PageEvent are automatically send if a Activity or a Fragment is visible and implement [PageTrackable].
     *
     * @param application Application
     */
    fun start(application: Application) {
        pageViewTracker = PageViewTracker(SRGAnalytics)
        application.registerComponentCallbacks(applicationCallback)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallBacks)
    }

    override fun sendPageView(pageView: PageView) {
        pageViewTracker?.sendPageView(pageView)
    }
}
