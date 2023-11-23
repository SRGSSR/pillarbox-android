/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.SearchViewModel
import ch.srgssr.pillarbox.demo.trackPagView
import ch.srgssr.pillarbox.demo.ui.examples.ExamplesHome
import ch.srgssr.pillarbox.demo.ui.integrationLayer.SearchView
import ch.srgssr.pillarbox.demo.ui.integrationLayer.listNavGraph
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.showcases.showCasesNavGraph

private val bottomNavItems = listOf(HomeDestination.Examples, HomeDestination.ShowCases, HomeDestination.Lists, HomeDestination.Search)

/**
 * Main view with all the navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("StringLiteralDuplication")
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
        val context = LocalContext.current
        val ilRepository = remember {
            PlayerModule.createIlRepository(context)
        }
        NavHost(navController = navController, startDestination = HomeDestination.Examples.route, modifier = Modifier.padding(innerPadding)) {
            composable(HomeDestination.Examples.route, DemoPageView("home", listOf("app", "pillarbox", "examples"))) {
                ExamplesHome()
            }

            navigation(startDestination = NavigationRoutes.showcaseList, route = HomeDestination.ShowCases.route) {
                showCasesNavGraph(navController)
            }

            navigation(startDestination = NavigationRoutes.contentLists, route = HomeDestination.Lists.route) {
                listNavGraph(navController, ilRepository)
            }

            composable(HomeDestination.Info.route, DemoPageView("home", listOf("app", "pillarbox", "information"))) {
                InfoView()
            }

            composable(route = NavigationRoutes.searchHome, DemoPageView("home", listOf("app", "pillarbox", "search"))) {
                val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(ilRepository))
                SearchView(searchViewModel = viewModel) {
                    val item = DemoItem(
                        title = it.title,
                        uri = it.urn,
                        description = it.description,
                        imageUrl = it.imageUrl
                    )

                    SimplePlayerActivity.startActivity(context, item)
                }
            }
        }
    }
}

@Composable
private fun DemoBottomNavigation(navController: NavController, currentDestination: NavDestination?) {
    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = screen.imageVector, contentDescription = null)
                },
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
    pageView: DemoPageView,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route = route, arguments = arguments, deepLinks = deepLinks) {
        val entryLifecycle = it
        LaunchedEffect(pageView) {
            entryLifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                SRGAnalytics.trackPagView(pageView)
            }
        }
        content.invoke(it)
    }
}
