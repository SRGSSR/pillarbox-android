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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.examples.ExamplesHome
import ch.srgssr.pillarbox.demo.ui.showcases.showCasesNavGraph

private val bottomNavItems = listOf(HomeDestination.Examples, HomeDestination.ShowCases, HomeDestination.Info)

/**
 * Main view with all the navigation
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = currentDestination.getLabelResId())) })
        },
        bottomBar = {
            BottomNavigation {
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
        NavHost(navController = navController, startDestination = HomeDestination.Examples.route, modifier = Modifier.padding(innerPadding)) {
            composable(HomeDestination.Examples.route) {
                ExamplesHome()
            }

            navigation(startDestination = NavigationRoutes.showcaseList, route = HomeDestination.ShowCases.route) {
                showCasesNavGraph(navController)
            }

            composable(HomeDestination.Info.route) {
                InfoView()
            }
        }
    }
}

private fun NavDestination?.getLabelResId(): Int {
    val navItem: HomeDestination? = this?.let {
        for (item in bottomNavItems) {
            if (hierarchy.any { it.route == item.route }) {
                return@let item
            }
        }
        null
    }
    return navItem?.labelResId ?: R.string.app_name
}
