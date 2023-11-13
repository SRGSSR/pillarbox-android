/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.comscore.ComScorePageView

/**
 * Demo page view that create the ComScore and CommandersAct page view object.
 *
 * @property title ComScore and CommandersAct title
 * @property levels CommandersAct levels
 */
data class DemoPageView(val title: String, val levels: List<String> = emptyList())

/**
 * Extension to track page view from a [DemoPageView]
 *
 * @param demoPageView
 */
fun SRGAnalytics.trackPagView(demoPageView: DemoPageView) {
    val comScorePageView = ComScorePageView(name = demoPageView.title)
    val commandersActPageView = CommandersActPageView(name = demoPageView.title, type = "DemoScreen", levels = demoPageView.levels)
    sendPageView(commandersAct = commandersActPageView, comScore = comScorePageView)
}
