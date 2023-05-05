/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Page view tracker send page views only if the last page view is different.
 * It will send again the last page view when application come back to foreground.
 *
 * @property pageViewAnalytics The [PageViewAnalytics] implementation to send page views.
 */
class PageViewTracker(private val pageViewAnalytics: PageViewAnalytics) : PageViewAnalytics {
    private var lastPageView: PageView? = null

    init {
        val processLifecycleOwner = ProcessLifecycleOwner.get()
        processLifecycleOwner.lifecycleScope.launch {
            processLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val lastPageView = clear()
                lastPageView?.let {
                    sendPageView(it)
                }
            }
        }
    }

    override fun sendPageView(pageView: PageView) {
        if (lastPageView != pageView) {
            pageViewAnalytics.sendPageView(pageView)
            lastPageView = pageView
        }
    }

    /**
     * Clear [lastPageView]
     */
    fun clear(): PageView? {
        val output = lastPageView
        lastPageView = null
        return output
    }
}
