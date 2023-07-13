/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * ComScore
 *
 * @constructor Create empty Com score
 */
interface ComScore {
    /**
     * Send page view to ComScore
     * @param title The title of the page stored in c8.
     */
    fun sendPageView(title: String)
}
