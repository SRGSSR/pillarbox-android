/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ch.srgssr.pillarbox.demo.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.showcases.adaptive.AdaptivePlayerHome
import ch.srgssr.pillarbox.demo.ui.showcases.multiplayer.MultiPlayer
import ch.srgssr.pillarbox.demo.ui.showcases.story.StoryHome

/**
 * Inject Showcases Navigation
 */
fun NavGraphBuilder.showCasesNavGraph(navController: NavController) {
    composable(NavigationRoutes.showcaseList) {
        ShowCaseList(navController = navController)
    }
    composable(NavigationRoutes.story) {
        StoryHome()
    }
    composable(NavigationRoutes.simplePlayer) {
        SimplePlayerIntegration()
    }
    composable(NavigationRoutes.adaptive) {
        AdaptivePlayerHome()
    }
    composable(NavigationRoutes.playerSwap) {
        MultiPlayer()
    }
    composable(NavigationRoutes.exoPlayerSample) {
        ExoPlayerSample()
    }
}
