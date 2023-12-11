/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import ch.srgssr.pillarbox.demo.shared.R

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
    data object Search : HomeDestination(NavigationRoutes.searchHome, R.string.search, Icons.Default.Search)
}

/**
 * Navigate as a top level destination.
 *
 * @param destination The [HomeDestination] to navigate to.
 */
fun NavController.navigate(destination: HomeDestination) {
    navigate(destination.route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}
