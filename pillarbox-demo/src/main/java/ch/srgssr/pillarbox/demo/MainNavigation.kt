/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.content.Context
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
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
    listOf(HomeDestination.Examples, HomeDestination.Showcases, HomeDestination.Lists, HomeDestination.Search, HomeDestination.Settings)
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
    var forceSAM by remember { mutableStateOf(false) }
    var ilLocation by remember { mutableStateOf<IlLocation?>(null) }

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
                    currentDestination?.let { currentDestination ->
                        if (currentDestination.hasRoute<NavigationRoutes.ContentLists>()) {
                            ListsMenu(
                                currentServer = ilHost,
                                currentForceSAM = forceSAM,
                                currentLocation = ilLocation,
                                onServerSelected = { host, forceSam, location ->
                                    ilHost = host
                                    forceSAM = forceSam
                                    ilLocation = location
                                },
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            DemoBottomNavigation(navController = navController, currentDestination = currentDestination)
        }
    ) { innerPadding ->
        val context = LocalContext.current
        val listsIlRepository = remember(ilHost, forceSAM, ilLocation) {
            PlayerModule.createIlRepository(context, ilHost, forceSAM, ilLocation)
        }

        NavHost(navController = navController, startDestination = NavigationRoutes.HomeSamples, modifier = Modifier.padding(innerPadding)) {
            composable<NavigationRoutes.HomeSamples>(DemoPageView("home", listOf("app", "pillarbox", "examples"))) {
                ExamplesHome()
            }

            navigation<NavigationRoutes.HomeShowcases>(NavigationRoutes.ShowcaseList) {
                showcasesNavGraph(navController)
            }

            navigation<NavigationRoutes.HomeLists>(NavigationRoutes.ContentLists) {
                listsNavGraph(navController, listsIlRepository, ilHost, forceSAM, ilLocation)
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
                    val item = DemoItem.URN(
                        title = it.title,
                        urn = it.urn,
                        description = it.description,
                        imageUri = it.imageUrl.decorated(width = ImageWidth.W480),
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
    currentForceSAM: Boolean,
    currentLocation: IlLocation?,
    onServerSelected: (server: URL, forceSAM: Boolean, location: IlLocation?) -> Unit
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
        val context = LocalContext.current
        val currentServerUrl = currentServer.toString()
        val servers = remember { getServers(context).groupBy { it.serverName }.values }

        servers.forEachIndexed { index, environmentConfig ->
            environmentConfig.forEach { config ->
                val isSelected = currentServerUrl == config.host.toString() &&
                    currentForceSAM == config.forceSAM &&
                    currentLocation == config.location

                DropdownMenuItem(
                    text = { Text(text = config.displayName) },
                    onClick = {
                        onServerSelected(config.host, config.forceSAM, config.location)
                        isMenuVisible = false
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }

            if (index < servers.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = MaterialTheme.paddings.small),
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

internal fun getServers(context: Context): List<EnvironmentConfig> {
    val ilServers = listOf(null, IlLocation.CH, IlLocation.WW).flatMap { location ->
        val name = location?.let { "IL ($location)" } ?: "IL"

        listOf(
            EnvironmentConfig(
                name = context.getString(R.string.production),
                serverName = name,
                host = IlHost.PROD,
                location = location,
            ),
            EnvironmentConfig(
                name = context.getString(R.string.stage),
                serverName = name,
                host = IlHost.STAGE,
                location = location,
            ),
            EnvironmentConfig(
                name = context.getString(R.string.test),
                serverName = name,
                host = IlHost.TEST,
                location = location,
            ),
        )
    }
    val samServer = listOf(
        EnvironmentConfig(
            name = context.getString(R.string.production),
            serverName = "SAM",
            host = IlHost.PROD,
            forceSAM = true,
        ),
        EnvironmentConfig(
            name = context.getString(R.string.stage),
            serverName = "SAM",
            host = IlHost.STAGE,
            forceSAM = true,
        ),
        EnvironmentConfig(
            name = context.getString(R.string.test),
            serverName = "SAM",
            host = IlHost.TEST,
            forceSAM = true,
        )
    )

    return ilServers + samServer
}

internal data class EnvironmentConfig(
    val name: String,
    val serverName: String,
    val host: URL,
    val forceSAM: Boolean = false,
    val location: IlLocation? = null,
) {
    val displayName: String
        get() = "$serverName - $name"
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
            currentForceSAM = false,
            currentLocation = null,
            onServerSelected = { _, _, _ -> },
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
