/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import ch.srgssr.pillarbox.analytics.PageView
import ch.srgssr.pillarbox.analytics.SRGPageViewTracker
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
            DemoBottomNavigation(navController = navController, currentDestination = currentDestination)
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = HomeDestination.Examples.route, modifier = Modifier.padding(innerPadding)) {
            composable(HomeDestination.Examples.route, PageView("home", arrayOf("app", "pillarbox", "examples"))) {
                ExamplesHome()
            }

            navigation(startDestination = NavigationRoutes.showcaseList, route = HomeDestination.ShowCases.route) {
                showCasesNavGraph(navController)
            }

            composable(HomeDestination.Info.route, PageView("home", arrayOf("app", "pillarbox", "information"))) {
                InfoView()
            }
        }
    }
}

private fun displayBottomBar(currentDestination: NavDestination?): Boolean {
    return when (currentDestination?.route) {
        HomeDestination.Examples.route -> true
        HomeDestination.Info.route -> true
        NavigationRoutes.showcaseList -> true
        else -> false
    }
}

@Composable
private fun DemoBottomNavigation(navController: NavController, currentDestination: NavDestination?) {
    val bottomBarState = rememberSaveable {
        mutableStateOf(true)
    }
    bottomBarState.value = displayBottomBar(currentDestination)
    AnimatedVisibility(
        visible = bottomBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
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

/**
 * Add the Composable to the NavGraphBuilder
 *
 * @param route route for the destination
 * @param pageView page view to send to [SRGPageViewTracker]
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param content composable for the destination
 * @receiver
 */
fun NavGraphBuilder.composable(
    route: String,
    pageView: PageView,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route = route, arguments = arguments, deepLinks = deepLinks) {
        SRGPageViewTracker.sendPageView(pageView)
        content.invoke(it)
    }
}
