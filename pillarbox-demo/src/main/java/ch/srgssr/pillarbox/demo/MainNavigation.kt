/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import ch.srg.dataProvider.integrationlayer.request.image.ImageWidth
import ch.srg.dataProvider.integrationlayer.request.image.decorated
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.SearchViewModel
import ch.srgssr.pillarbox.demo.shared.ui.navigateTopLevel
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsViewModel
import ch.srgssr.pillarbox.demo.ui.examples.ExamplesHome
import ch.srgssr.pillarbox.demo.ui.lists.listsNavGraph
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.search.SearchHome
import ch.srgssr.pillarbox.demo.ui.settings.AppSettingsView
import ch.srgssr.pillarbox.demo.ui.showcases.showcasesNavGraph
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import java.net.URL

private val bottomNavItems =
    listOf(HomeDestination.Examples, HomeDestination.ShowCases, HomeDestination.Lists, HomeDestination.Search, HomeDestination.Settings)
private val topLevelRoutes = listOf(
    NavigationRoutes.HomeSamples,
    NavigationRoutes.ShowcaseList,
    NavigationRoutes.ContentLists,
    NavigationRoutes.SearchHome,
    NavigationRoutes.SettingsHome,
)

/**
 * Main view with all the navigation
 */
@Suppress("StringLiteralDuplication")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var ilHost by remember { mutableStateOf(IlHost.DEFAULT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    currentDestination?.let {
                        Text(text = stringResource(id = currentDestination.getLabelResId()))
                    }
                },
                navigationIcon = {
                    currentDestination?.let { currentDestination ->
                        if (topLevelRoutes.none { currentDestination.hasRoute(it::class) }) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_up)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (currentDestination?.hasRoute(NavigationRoutes.ContentLists::class) == true) {
                        ListsMenu(
                            currentServer = ilHost,
                            onServerSelected = { ilHost = it }
                        )
                    }
                }
            )
        },
        bottomBar = {
            DemoBottomNavigation(navController = navController, currentDestination = currentDestination)
        }
    ) { innerPadding ->
        val context = LocalContext.current

        NavHost(navController = navController, startDestination = NavigationRoutes.HomeSamples, modifier = Modifier.padding(innerPadding)) {
            composable<NavigationRoutes.HomeSamples>(DemoPageView("home", listOf("app", "pillarbox", "examples"))) {
                ExamplesHome()
            }

            navigation<NavigationRoutes.HomeShowcases>(NavigationRoutes.ShowcaseList) {
                showcasesNavGraph(navController)
            }

            navigation<NavigationRoutes.HomeLists>(NavigationRoutes.ContentLists) {
                val ilRepository = PlayerModule.createIlRepository(context, ilHost)

                listsNavGraph(navController, ilRepository, ilHost)
            }

            composable<NavigationRoutes.SettingsHome>(DemoPageView("home", listOf("app", "pillarbox", "settings"))) {
                val appSettingsRepository = remember(context) {
                    AppSettingsRepository(context)
                }

                val appSettingsViewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.Factory(appSettingsRepository))
                AppSettingsView(appSettingsViewModel)
            }

            composable<NavigationRoutes.SearchHome>(DemoPageView("home", listOf("app", "pillarbox", "search"))) {
                val ilRepository = PlayerModule.createIlRepository(context)
                val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(ilRepository))
                SearchHome(searchViewModel = viewModel) {
                    val item = DemoItem(
                        title = it.title,
                        uri = it.urn,
                        description = it.description,
                        imageUrl = it.imageUrl.decorated(width = ImageWidth.W480)
                    )

                    SimplePlayerActivity.startActivity(context, item)
                }
            }
        }
    }
}

@Composable
private fun ListsMenu(
    currentServer: URL,
    onServerSelected: (server: URL) -> Unit
) {
    var isMenuVisible by remember { mutableStateOf(false) }

    IconButton(onClick = { isMenuVisible = true }) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.server)
        )
    }

    DropdownMenu(
        expanded = isMenuVisible,
        onDismissRequest = { isMenuVisible = false },
        offset = DpOffset(
            x = MaterialTheme.paddings.small,
            y = 0.dp,
        ),
    ) {
        val currentServerUrl = currentServer.toString()
        val servers = mapOf(
            stringResource(R.string.production) to IlHost.PROD.toString(),
            stringResource(R.string.stage) to IlHost.STAGE.toString(),
            stringResource(R.string.test) to IlHost.TEST.toString()
        )

        Text(
            text = stringResource(R.string.server),
            modifier = Modifier
                .padding(MenuDefaults.DropdownMenuItemContentPadding)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelMedium
        )

        servers.forEach { (name, url) ->
            DropdownMenuItem(
                text = { Text(text = name) },
                onClick = {
                    onServerSelected(URL(url))
                    isMenuVisible = false
                },
                trailingIcon = if (currentServerUrl == url) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                } else {
                    null
                }
            )
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
                selected = currentDestination?.hierarchy?.any { it.hasRoute(screen.route::class) } == true,
                onClick = {
                    navController.navigateTopLevel(screen.route)
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ListsMenuPreview() {
    PillarboxTheme {
        ListsMenu(
            currentServer = IlHost.PROD,
            onServerSelected = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun DemoBottomNavigationPreview() {
    PillarboxTheme {
        DemoBottomNavigation(
            navController = rememberNavController(),
            currentDestination = null
        )
    }
}

private fun NavDestination.getLabelResId(): Int {
    val navItem: HomeDestination? = bottomNavItems.firstOrNull { destination ->
        hierarchy.any { it.hasRoute(destination.route::class) }
    }
    return navItem?.labelResId ?: ResourcesCompat.ID_NULL
}

internal inline fun <reified T : Any> NavGraphBuilder.composable(
    pageView: DemoPageView,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T> {
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME, it) {
            SRGAnalytics.trackPagView(pageView)
        }
        content(it)
    }
}
