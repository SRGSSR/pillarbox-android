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
import ch.srgssr.pillarbox.demo.ui.showcases.integrations.Media3ComposeSample
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.ChapterShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.SimpleLayoutShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.StoryLayoutShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.layouts.thumbnail.ThumbnailView
import ch.srgssr.pillarbox.demo.ui.showcases.misc.ContentNotYetAvailable
import ch.srgssr.pillarbox.demo.ui.showcases.misc.MultiPlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.ResizablePlayerShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.SmoothSeekingShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.SphericalSurfaceShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.StartAtGivenTimeShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.TimeBasedContent
import ch.srgssr.pillarbox.demo.ui.showcases.misc.TrackingToggleShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.misc.UpdatableMediaItemShowcase
import ch.srgssr.pillarbox.demo.ui.showcases.playlists.CustomPlaybackSettingsShowcase

/**
 * Inject Showcases Navigation
 */
fun NavGraphBuilder.showcasesNavGraph(navController: NavController) {
    composable<NavigationRoutes.ShowcaseList>(DemoPageView("home", Levels)) {
        ShowcasesHome(navController = navController)
    }
    composable<NavigationRoutes.ShowcasePlaybackSettings>(DemoPageView("playback settings", Levels)) {
        CustomPlaybackSettingsShowcase(playlist = Playlist.VideoUrns)
    }
    composable<NavigationRoutes.Story>(DemoPageView("story", Levels)) {
        StoryLayoutShowcase()
    }
    composable<NavigationRoutes.SimplePlayer>(DemoPageView("basic player", Levels)) {
        SimpleLayoutShowcase()
    }
    composable<NavigationRoutes.Adaptive>(DemoPageView("adaptive player", Levels)) {
        ResizablePlayerShowcase()
    }
    composable<NavigationRoutes.PlayerSwap>(DemoPageView("multiplayer", Levels)) {
        MultiPlayerShowcase()
    }
    composable<NavigationRoutes.ExoPlayerSample>(DemoPageView("exoplayer", Levels)) {
        ExoPlayerShowcase()
    }
    composable<NavigationRoutes.TrackingSample>(DemoPageView("tracking toggle", Levels)) {
        TrackingToggleShowcase()
    }
    composable<NavigationRoutes.UpdatableSample>(DemoPageView("updatable item", Levels)) {
        UpdatableMediaItemShowcase()
    }
    composable<NavigationRoutes.SmoothSeeking>(DemoPageView("smooth seeking", Levels)) {
        SmoothSeekingShowcase()
    }
    composable<NavigationRoutes.StartAtGivenTime>(DemoPageView("start at given time", Levels)) {
        StartAtGivenTimeShowcase()
    }
    composable<NavigationRoutes.Video360>(DemoPageView("Video 360°", Levels)) {
        SphericalSurfaceShowcase()
    }
    composable<NavigationRoutes.Chapters>(DemoPageView("Chapters", Levels)) {
        ChapterShowcase()
    }
    composable<NavigationRoutes.CountdownShowcase>(DemoPageView("CountdownShowcase", Levels)) {
        ContentNotYetAvailable()
    }
    composable<NavigationRoutes.ThumbnailShowcase>(DemoPageView("ThumbnailShowcase", Levels)) {
        ThumbnailView()
    }
    composable<NavigationRoutes.TimeBasedContent>(DemoPageView("TimeBasedContent", Levels)) {
        TimeBasedContent()
    }
    composable<NavigationRoutes.Media3ComposeSample>(DemoPageView("Media3ComposeSample", Levels)) {
        Media3ComposeSample()
    }
}

private val Levels = listOf("app", "pillarbox", "showcase")
