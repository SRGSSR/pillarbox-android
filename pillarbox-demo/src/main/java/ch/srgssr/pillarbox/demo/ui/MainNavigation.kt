/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.integrations.IntegrationsHome
import ch.srgssr.pillarbox.demo.ui.integrations.adaptive.AdaptivePlayerHome
import ch.srgssr.pillarbox.demo.ui.integrations.multiplayer.PlayerSwap
import ch.srgssr.pillarbox.demo.ui.integrations.story.StoryHome
import ch.srgssr.pillarbox.demo.ui.integrations.streams.StreamHome

private val bottomNavItems = listOf(HomeDestination.Streams, HomeDestination.Integrations, HomeDestination.Info)

/**
 * Main view with all the navigation
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) })
        },
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Image(painter = painterResource(id = screen.iconResId), contentDescription = null) },
                        label = { Text(stringResource(screen.labelResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = HomeDestination.Streams.route, modifier = Modifier.padding(innerPadding)) {
            composable(HomeDestination.Streams.route) {
                StreamHome()
            }

            composable(HomeDestination.Integrations.route) {
                IntegrationsHome(navController)
            }

            composable(HomeDestination.Info.route) {
                InfoView()
            }

            composable(NavigationRoutes.story) {
                StoryHome()
            }
            composable(NavigationRoutes.adaptive) {
                AdaptivePlayerHome()
            }
            composable(NavigationRoutes.player_swap) {
                PlayerSwap()
            }
        }
    }
}
