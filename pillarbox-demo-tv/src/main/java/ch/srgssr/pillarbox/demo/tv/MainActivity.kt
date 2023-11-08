/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        Box {
                            val density = LocalDensity.current
                            val destinations = listOf(HomeDestination.Examples, HomeDestination.Lists)
                            val navController = rememberNavController()

                            var selectedDestination by remember { mutableStateOf(destinations[0]) }
                            var topBarHeight by remember { mutableStateOf(0.dp) }

                            TVDemoTopBar(
                                destinations = destinations,
                                selectedDestination = selectedDestination,
                                modifier = Modifier
                                    .onSizeChanged {
                                        topBarHeight = with(density) { it.height.toDp() }
                                    }
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
                                startDestination = HomeDestination.Examples,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = topBarHeight)
                                    .padding(horizontal = HorizontalPadding)
                            )
                        }
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
