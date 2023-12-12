/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.handleDPadKeyEvents

/**
 * Top bar displayed in the demo app on TV.
 *
 * @param destinations The list of destinations to display.
 * @param currentNavDestination The currently destination selected.
 * @param modifier The [Modifier] to apply to the top bar.
 * @param onDestinationClick The action to perform the selected a destination.
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
fun TVDemoTopBar(
    destinations: List<HomeDestination>,
    currentNavDestination: NavDestination?,
    modifier: Modifier = Modifier,
    onDestinationClick: (destination: HomeDestination) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusedTabIndex by rememberSaveable(currentNavDestination) {
        mutableIntStateOf(
            destinations.indexOfFirst { dest ->
                currentNavDestination?.hierarchy?.any {
                    it.route == dest.route
                } == true
            }
        )
    }

    TabRow(
        selectedTabIndex = focusedTabIndex,
        modifier = modifier
            .focusRestorer()
            .handleDPadKeyEvents(
                onRight = {
                    if (focusedTabIndex < destinations.lastIndex) {
                        focusManager.moveFocus(FocusDirection.Right)
                    }
                }
            )
    ) {
        destinations.forEachIndexed { index, destination ->
            key(index) {
                val focusRequester = remember {
                    FocusRequester()
                }
                LaunchedEffect(focusedTabIndex) {
                    if (index == focusedTabIndex) {
                        focusRequester.requestFocus()
                    }
                }
                Tab(
                    modifier = Modifier.focusRequester(focusRequester),
                    selected = focusedTabIndex == index,
                    onFocus = {
                        if (index != focusedTabIndex) {
                            onDestinationClick(destination)
                        }
                    },
                    onClick = { focusManager.moveFocus(FocusDirection.Down) }
                ) {
                    Text(
                        text = stringResource(destination.labelResId),
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.paddings.baseline,
                            vertical = MaterialTheme.paddings.small,
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TVDemoTopBarPreview() {
    PillarboxTheme {
        TVDemoTopBar(
            destinations = listOf(HomeDestination.Examples, HomeDestination.Lists, HomeDestination.Search),
            currentNavDestination = null,
            onDestinationClick = {}
        )
    }
}
