/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.ui.graphics.vector.ImageVector
import ch.srgssr.pillarbox.demo.R

/**
 * Home destinations
 *
 * @property route to navigate with NavController
 * @property labelResId string resource id
 * @property imageVector image vector
 */
sealed class HomeDestination(
    val route: String,
    @StringRes val labelResId: Int,
    val imageVector: ImageVector
) {
    /**
     * Examples home page containing all kind of streams
     */
    data object Examples : HomeDestination(NavigationRoutes.homeSamples, R.string.examples, Icons.Default.Home)

    /**
     * Streams home page
     */
    data object ShowCases : HomeDestination(NavigationRoutes.homeShowcases, R.string.showcases, Icons.Default.Movie)

    /**
     * Integration layer list home page
     */
    data object Lists : HomeDestination(NavigationRoutes.homeLists, R.string.lists, Icons.Default.ViewList)

    /**
     * Info home page
     */
    data object Info : HomeDestination(NavigationRoutes.homeInformation, R.string.info, Icons.Default.Info)

    /**
     * Info home page
     */
    data object Search : HomeDestination(NavigationRoutes.searchHome, R.string.search, Icons.Default.Search)
}
