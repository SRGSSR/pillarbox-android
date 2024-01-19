/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.composable
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.showcases.integrations.ExoPlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.SimpleLayoutShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.StoryLayoutShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.AdaptivePlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.MultiPlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.SmoothSeekingShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.StartAtGivenTimeShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.TrackingToggleShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.UpdatableMediaItemShowcase

/**
 * Inject Showcases Navigation
 */
fun NavGraphBuilder.showcasesNavGraph(navController: NavController) {
    composable(NavigationRoutes.showcaseList, DemoPageView("home", Levels)) {
        ShowcasesHome(navController = navController)
    }
    composable(NavigationRoutes.story, DemoPageView("story", Levels)) {
        StoryLayoutShowcase()
    }
    composable(NavigationRoutes.simplePlayer, DemoPageView("basic player", Levels)) {
        SimpleLayoutShowcase()
    }
    composable(NavigationRoutes.adaptive, DemoPageView("adaptive player", Levels)) {
        AdaptivePlayerShowcase()
    }
    composable(NavigationRoutes.playerSwap, DemoPageView("multiplayer", Levels)) {
        MultiPlayerShowcase()
    }
    composable(NavigationRoutes.exoPlayerSample, DemoPageView("exoplayer", Levels)) {
        ExoPlayerShowcase()
    }
    composable(NavigationRoutes.trackingSample, DemoPageView("tracking toggle", Levels)) {
        TrackingToggleShowcase()
    }
    composable(NavigationRoutes.updatableSample, DemoPageView("updatable item", Levels)) {
        UpdatableMediaItemShowcase()
    }
    composable(NavigationRoutes.smoothSeeking, DemoPageView("smooth seeking", Levels)) {
        SmoothSeekingShowcase()
    }
    composable(NavigationRoutes.startAtGivenTime, DemoPageView("start at given time", Levels)) {
        StartAtGivenTimeShowcase()
    }
}

private val Levels = listOf("app", "pillarbox", "showcase")
