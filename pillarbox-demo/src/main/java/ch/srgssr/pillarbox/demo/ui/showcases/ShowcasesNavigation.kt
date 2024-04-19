/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.composable
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.showcases.integrations.ExoPlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.SimpleLayoutShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.StoryLayoutShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.ChapterShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.MultiPlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.ResizablePlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.SmoothSeekingShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.SphericalSurfaceShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.StartAtGivenTimeShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.TrackingToggleShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.UpdatableMediaItemShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.playlists.CustomPlaybackSettingsShowcase

/**
 * Inject Showcases Navigation
 */
fun NavGraphBuilder.showcasesNavGraph(navController: NavController) {
    composable(NavigationRoutes.showcaseList, DemoPageView("home", Levels)) {
        ShowcasesHome(navController = navController)
    }
    composable(NavigationRoutes.showcasePlaybackSettings, DemoPageView("playback settings", Levels)) {
        CustomPlaybackSettingsShowcase(playlist = Playlist.VideoUrls)
    }
    composable(NavigationRoutes.story, DemoPageView("story", Levels)) {
        StoryLayoutShowcase()
    }
    composable(NavigationRoutes.simplePlayer, DemoPageView("basic player", Levels)) {
        SimpleLayoutShowcase()
    }
    composable(NavigationRoutes.adaptive, DemoPageView("adaptive player", Levels)) {
        ResizablePlayerShowcase()
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
    composable(NavigationRoutes.video360, DemoPageView("Video 360°", Levels)) {
        SphericalSurfaceShowcase()
    }
    composable(NavigationRoutes.chapters, DemoPageView("Chapters", Levels)) {
        ChapterShowcase()
    }
}

private val Levels = listOf("app", "pillarbox", "showcase")
