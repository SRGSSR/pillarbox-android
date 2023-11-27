/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.DemoPageView
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
import androidx.appcompat.R as appcompatR

/**
 * Main view with all the navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("StringLiteralDuplication")
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val bottomNavItems = remember {
        mutableStateListOf(HomeDestination.Examples, HomeDestination.ShowCases, HomeDestination.Lists, HomeDestination.Search)
    }
    val startDestination by remember(bottomNavItems) { mutableStateOf(bottomNavItems[0]) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var activeBottomItem by remember(startDestination) { mutableStateOf(startDestination) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(activeBottomItem.labelResId)) },
                navigationIcon = {
                    val currentRoute = navBackStackEntry?.destination?.route
                    val isSubScreen = currentRoute != null && activeBottomItem.route != currentRoute

                    if (isSubScreen) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(appcompatR.string.abc_action_bar_up_description)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            DemoBottomNavigation(
                items = bottomNavItems,
                activeItem = activeBottomItem,
                onItemClick = { item ->
                    activeBottomItem = item

                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(startDestination.route) {
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
    ) { innerPadding ->
        val context = LocalContext.current
        val ilRepository = remember {
            PlayerModule.createIlRepository(context)
        }
        NavHost(navController = navController, startDestination = startDestination.route, modifier = Modifier.padding(innerPadding)) {
            composable(HomeDestination.Examples.route, DemoPageView("home", listOf("app", "pillarbox", "examples"))) {
                ExamplesHome()
            }

            navigation(startDestination = HomeDestination.ShowCases.route, route = NavigationRoutes.showcaseList) {
                showCasesNavGraph(navController)
            }

            navigation(startDestination = HomeDestination.Lists.route, route = NavigationRoutes.contentLists) {
                listNavGraph(navController, ilRepository)
            }

            composable(HomeDestination.Info.route, DemoPageView("home", listOf("app", "pillarbox", "information"))) {
                InfoView()
            }

            composable(route = HomeDestination.Search.route, DemoPageView("home", listOf("app", "pillarbox", "search"))) {
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
private fun DemoBottomNavigation(
    items: List<HomeDestination>,
    activeItem: HomeDestination,
    onItemClick: (item: HomeDestination) -> Unit
) {
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = screen.imageVector, contentDescription = null)
                },
                label = { Text(stringResource(screen.labelResId)) },
                selected = screen == activeItem,
                onClick = { onItemClick(screen) }
            )
        }
    }
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
