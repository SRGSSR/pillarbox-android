/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .focusRestorer(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
        verticalAlignment = Alignment.CenterVertically
    ) {
        destinations.forEach { destination ->
            ListItem(
                selected = destination == selectedDestination,
                onClick = { onDestinationClick(destination) },
                modifier = Modifier.width(IntrinsicSize.Max),
                headlineContent = {
                    Text(text = stringResource(destination.labelResId))
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ListItem(
            selected = selectedDestination == HomeDestination.Search,
            onClick = { onDestinationClick(HomeDestination.Search) },
            modifier = Modifier.width(IntrinsicSize.Max),
            shape = ListItemDefaults.shape(CircleShape),
            headlineContent = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(HomeDestination.Search.labelResId)
                )
            }
        )
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
