/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.tv.ui.TVDemoNavigation
import ch.srgssr.pillarbox.demo.tv.ui.TVDemoTopBar
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PillarboxTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        val destinations = listOf(HomeDestination.Examples, HomeDestination.Lists)
                        val navController = rememberNavController()
                        val startDestination by remember(destinations) { mutableStateOf(destinations[0]) }

                        var selectedDestination by remember { mutableStateOf(startDestination) }

                        TVDemoTopBar(
                            destinations = destinations,
                            selectedDestination = selectedDestination,
                            modifier = Modifier
                                .padding(
                                    horizontal = HorizontalPadding,
                                    vertical = VerticalPadding
                                ),
                            onDestinationSelected = {
                                selectedDestination = it

                                navController.navigate(it.route)
                            }
                        )

                        TVDemoNavigation(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = HorizontalPadding)
                        )
                    }
                }
            }
        }
    }

    private companion object {
        private val HorizontalPadding = 58.dp
        private val VerticalPadding = 16.dp
    }
}
