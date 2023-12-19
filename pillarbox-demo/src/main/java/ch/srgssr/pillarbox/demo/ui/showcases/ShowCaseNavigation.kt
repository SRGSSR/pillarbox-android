/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.composable
import ch.srgssr.pillarbox.demo.ui.showcases.adaptive.AdaptivePlayerHome
import ch.srgssr.pillarbox.demo.ui.showcases.multiplayer.MultiPlayer
import ch.srgssr.pillarbox.demo.ui.showcases.story.StoryHome
import ch.srgssr.pillarbox.demo.ui.showcases.tracking.TrackingToggleSample
import ch.srgssr.pillarbox.demo.ui.showcases.updatable.UpdatableMediaItemView
import ch.srgssr.pillarbox.demo.ui.showcases.webview.WebViewShowcase

/**
 * Inject Showcases Navigation
 */
fun NavGraphBuilder.showCasesNavGraph(navController: NavController) {
    composable(NavigationRoutes.showcaseList, DemoPageView("home", Levels)) {
        ShowCaseList(navController = navController)
    }
    composable(NavigationRoutes.story, DemoPageView("story", Levels)) {
        StoryHome()
    }
    composable(NavigationRoutes.simplePlayer, DemoPageView("basic player", Levels)) {
        SimplePlayerIntegration()
    }
    composable(NavigationRoutes.adaptive, DemoPageView("adaptive player", Levels)) {
        AdaptivePlayerHome()
    }
    composable(NavigationRoutes.playerSwap, DemoPageView("multiplayer", Levels)) {
        MultiPlayer()
    }
    composable(NavigationRoutes.exoPlayerSample, DemoPageView("exoplayer", Levels)) {
        ExoPlayerSample()
    }
    composable(NavigationRoutes.trackingSample, DemoPageView("tracking toggle", Levels)) {
        TrackingToggleSample()
    }
    composable(NavigationRoutes.updatableSample, DemoPageView("updatable item", Levels)) {
        UpdatableMediaItemView()
    }
    composable(route = NavigationRoutes.webViewSample) {
        WebViewShowcase()
    }
}

private val Levels = listOf("app", "pillarbox", "showcase")
