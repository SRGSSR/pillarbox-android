/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.MaterialTheme
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.SearchViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.tv.ui.examples.ExamplesHome
import ch.srgssr.pillarbox.demo.tv.ui.lists.ListsHome
import ch.srgssr.pillarbox.demo.tv.ui.player.PlayerActivity
import ch.srgssr.pillarbox.demo.tv.ui.search.SearchHome
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings

/**
 * The nav host of the demo app on TV.
 *
 * @param navController The [NavHostController] used to navigate between screens.
 * @param startDestination The start destination to display.
 * @param modifier The [Modifier] to apply to the [NavHost].
 */
@Composable
fun MainNavigation(
    navController: NavHostController,
    startDestination: HomeDestination,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(HomeDestination.Examples.route) {
            val context = LocalContext.current

            ExamplesHome(
                onItemSelected = { PlayerActivity.startPlayer(context, it) }
            )
        }

        composable(HomeDestination.Lists.route) {
            ListsHome(
                sections = contentListSections
            )
        }

        composable(HomeDestination.Search.route) {
            val context = LocalContext.current
            val ilRepository = remember {
                PlayerModule.createIlRepository(context)
            }

            val searchViewModel = viewModel<SearchViewModel>(
                factory = SearchViewModel.Factory(ilRepository)
            )

            SearchHome(
                searchViewModel = searchViewModel,
                modifier = Modifier.padding(top = MaterialTheme.paddings.baseline)
            )
        }
    }
}

@Preview
@Composable
private fun MainNavigationPreview() {
    PillarboxTheme {
        MainNavigation(
            navController = rememberNavController(),
            startDestination = HomeDestination.Examples
        )
    }
}
