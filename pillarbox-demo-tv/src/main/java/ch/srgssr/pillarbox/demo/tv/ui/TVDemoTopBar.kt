/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings

/**
 * Top bar displayed in the demo app on TV.
 *
 * @param destinations The list of destinations to display.
 * @param selectedDestination The currently selected destination.
 * @param modifier The [Modifier] to apply to the top bar.
 * @param onDestinationClick The action to perform the selected a destination.
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
fun TVDemoTopBar(
    destinations: List<HomeDestination>,
    selectedDestination: HomeDestination,
    modifier: Modifier = Modifier,
    onDestinationClick: (destination: HomeDestination) -> Unit
) {
    var focusedTabIndex by remember(selectedDestination) {
        mutableIntStateOf(destinations.indexOf(selectedDestination))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabRow(
            selectedTabIndex = focusedTabIndex,
            modifier = Modifier
                .focusRestorer()
                .onFocusChanged {
                    if (!it.hasFocus) {
                        focusedTabIndex = destinations.indexOf(selectedDestination)
                    }
                },
        ) {
            destinations.forEachIndexed { index, destination ->
                Tab(
                    selected = index == focusedTabIndex,
                    onFocus = { focusedTabIndex = index },
                    onClick = { onDestinationClick(destination) },
                ) {
                    Text(
                        text = stringResource(destination.labelResId),
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.paddings.baseline,
                            vertical = MaterialTheme.paddings.small,
                        ),
                    )
                }
            }
        }

        IconButton(
            onClick = { onDestinationClick(HomeDestination.Search) },
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(HomeDestination.Search.labelResId),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TVDemoTopBarPreview() {
    PillarboxTheme {
        TVDemoTopBar(
            destinations = listOf(HomeDestination.Examples, HomeDestination.Lists),
            selectedDestination = HomeDestination.Examples,
            onDestinationClick = {}
        )
    }
}
