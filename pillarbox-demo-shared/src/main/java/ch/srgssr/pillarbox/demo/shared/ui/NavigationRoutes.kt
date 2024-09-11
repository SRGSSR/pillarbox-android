/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui

import kotlinx.serialization.Serializable

/**
 * Navigation stores all routes available
 */
@Serializable
@Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
sealed interface NavigationRoutes {
    @Serializable
    data object HomeSamples : NavigationRoutes

    @Serializable
    data class HomeSample(val index: Int) : NavigationRoutes

    @Serializable
    data object HomeShowcases : NavigationRoutes

    @Serializable
    data object ShowcaseList : NavigationRoutes

    @Serializable
    data object ShowcasePlaybackSettings : NavigationRoutes

    @Serializable
    data object Story : NavigationRoutes

    @Serializable
    data object SimplePlayer : NavigationRoutes

    @Serializable
    data object Adaptive : NavigationRoutes

    @Serializable
    data object PlayerSwap : NavigationRoutes

    @Serializable
    data object ExoPlayerSample : NavigationRoutes

    @Serializable
    data object TrackingSample : NavigationRoutes

    @Serializable
    data object UpdatableSample : NavigationRoutes

    @Serializable
    data object SmoothSeeking : NavigationRoutes

    @Serializable
    data object StartAtGivenTime : NavigationRoutes

    @Serializable
    data object Video360 : NavigationRoutes

    @Serializable
    data object Chapters : NavigationRoutes

    @Serializable
    data object HomeLists : NavigationRoutes

    @Serializable
    data object ContentLists : NavigationRoutes

    @Serializable
    data class ContentList(val index: Int) : NavigationRoutes

    @Serializable
    data object SearchHome : NavigationRoutes

    @Serializable
    data object SettingsHome : NavigationRoutes
}
