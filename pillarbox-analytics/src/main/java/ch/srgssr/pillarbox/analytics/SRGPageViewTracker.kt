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
 * SRG Page view tracker that send page view to [SRGAnalytics].
 * Last page view is automatically send again when application come back from background.
 * Limitation : This class assume you have only one page view event per screen!
 */
object SRGPageViewTracker : PageViewAnalytics {
    private var pageViewTracker: PageViewTracker = PageViewTracker(SRGAnalytics)

    init {
        val processLifecycleOwner = ProcessLifecycleOwner.get()
        processLifecycleOwner.lifecycleScope.launch {
            processLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val lastPageView = pageViewTracker.clear()
                lastPageView?.let {
                    sendPageView(it)
                }
            }
        }
    }

    override fun sendPageView(pageView: PageView) {
        pageViewTracker.sendPageView(pageView)
    }
}
