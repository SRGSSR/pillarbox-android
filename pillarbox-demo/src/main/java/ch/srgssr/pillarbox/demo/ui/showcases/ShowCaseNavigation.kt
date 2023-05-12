/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ch.srgssr.pillarbox.analytics.PageView
import ch.srgssr.pillarbox.demo.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.composable
import ch.srgssr.pillarbox.demo.ui.showcases.adaptive.AdaptivePlayerHome
import ch.srgssr.pillarbox.demo.ui.showcases.multiplayer.MultiPlayer
import ch.srgssr.pillarbox.demo.ui.showcases.story.StoryHome

/**
 * Inject Showcases Navigation
 */
fun NavGraphBuilder.showCasesNavGraph(navController: NavController) {
    composable(NavigationRoutes.showcaseList, PageView("home", Levels)) {
        ShowCaseList(navController = navController)
    }
    composable(NavigationRoutes.story, PageView("story", Levels)) {
        StoryHome()
    }
    composable(NavigationRoutes.simplePlayer, PageView("basic player", Levels)) {
        SimplePlayerIntegration()
    }
    composable(NavigationRoutes.adaptive, PageView("adaptive player", Levels)) {
        AdaptivePlayerHome()
    }
    composable(NavigationRoutes.playerSwap, PageView("multiplayer", Levels)) {
        MultiPlayer()
    }
    composable(NavigationRoutes.exoPlayerSample, PageView("exoplayer", Levels)) {
        ExoPlayerSample()
    }
}

private val Levels = arrayOf("app", "pillarbox", "showcase")
