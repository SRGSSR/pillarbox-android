/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ComScore Starter
 *
 * Track Activity lifecycle to ensure comScore start only when at least one Activity has been created.
 * It avoids comScore ghost start issue.
 *
 * For example, Application is created in background by UrbanAirship or other method without UI presented to user.
 *
 * No need to use this class if integrator call SRGAnalytics.init inside Application.onCreate.
 */
object ComScoreStarter : Application.ActivityLifecycleCallbacks {
    private val _uiExperienceStarted = AtomicBoolean(false)

    /**
     * Ui experience started
     *
     * At least one Activity was created!
     */
    val uiExperienceStarted get() = _uiExperienceStarted.get()

    /**
     * Start tracking activity
     *
     * Should be called inside your Application.onCreate
     *
     * @param application The application to track Activity lifecycle.
     */
    fun startTrackingActivity(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        _uiExperienceStarted.set(true)
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
