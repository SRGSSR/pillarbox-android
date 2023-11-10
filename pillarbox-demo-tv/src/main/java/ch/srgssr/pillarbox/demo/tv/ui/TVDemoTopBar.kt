/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ListItem
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme

/**
 * Top bar displayed in the demo app on TV.
 *
 * @param destinations The list of destinations to display.
 * @param selectedDestination The currently selected destination.
 * @param modifier The [Modifier] to apply to the top bar.
 * @param onDestinationSelected The action to perform the selected a destination.
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
fun TVDemoTopBar(
    destinations: List<HomeDestination>,
    selectedDestination: HomeDestination,
    modifier: Modifier = Modifier,
    onDestinationSelected: (destination: HomeDestination) -> Unit
) {
    var isTabRowFocused by remember { mutableStateOf(false) }

    TvLazyRow(
        modifier = modifier
            .fillMaxWidth()
            .focusRestorer()
            .onFocusChanged { isTabRowFocused = it.isFocused || it.hasFocus },
        contentPadding = PaddingValues(
            horizontal = 58.dp,
            vertical = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(destinations) { destination ->
            ListItem(
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
                modifier = Modifier.width(IntrinsicSize.Max),
                headlineContent = {
                    Text(text = stringResource(destination.labelResId))
                }
            )
        }
    }
}

@Preview
@Composable
private fun TVDemoTopBarPreview() {
    PillarboxTheme {
        TVDemoTopBar(
            destinations = listOf(HomeDestination.Examples, HomeDestination.Lists),
            selectedDestination = HomeDestination.Examples,
            onDestinationSelected = {}
        )
    }
}
