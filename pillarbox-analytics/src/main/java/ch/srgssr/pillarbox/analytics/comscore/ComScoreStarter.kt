/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * ComScore Starter
 *
 * Track Activity lifecycle to ensure comScore start only when at least one Activity has been created.
 * It avoids comScore ghost start issue.
 */
internal object ComScoreStarter : Application.ActivityLifecycleCallbacks {

    internal fun startTrackingActivity(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.application.unregisterActivityLifecycleCallbacks(this)
        ComScore.start(activity.applicationContext)
    }

    override fun onActivityStarted(activity: Activity) {
        // Nothing
    }

    override fun onActivityResumed(activity: Activity) {
        // Nothing
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
