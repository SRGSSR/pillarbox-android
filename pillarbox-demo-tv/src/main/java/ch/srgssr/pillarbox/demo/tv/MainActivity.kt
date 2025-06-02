/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.shared.ui.navigateTopLevel
import ch.srgssr.pillarbox.demo.tv.ui.components.TVDemoTopBar
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PillarboxTheme {
                Surface(
                    colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 58.dp)
                    ) {
                        val destinations = listOf(HomeDestination.Examples, HomeDestination.Lists, HomeDestination.Search)
                        val navController = rememberNavController()
                        val focusRequester = remember { FocusRequester() }

                        val startDestination by remember(destinations) { mutableStateOf(destinations[0]) }
                        val currentBackStackEntry by navController.currentBackStackEntryAsState()

                        TVDemoTopBar(
                            destinations = destinations,
                            currentNavDestination = currentBackStackEntry?.destination,
                            modifier = Modifier
                                .padding(vertical = MaterialTheme.paddings.baseline)
                                .focusRequester(focusRequester),
                            onDestinationClick = { destination ->
                                navController.navigateTopLevel(destination.route)
                            }
                        )

                        MainNavigation(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.fillMaxSize()
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    }
                }
            }
        }
    }
}
