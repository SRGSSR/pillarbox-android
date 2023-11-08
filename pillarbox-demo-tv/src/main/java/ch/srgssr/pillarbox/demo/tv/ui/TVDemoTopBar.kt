/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.compose.ui.zIndex
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowScope
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination

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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .focusRestorer(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var isTabRowFocused by remember { mutableStateOf(false) }
        val selectedDestinationIndex = destinations.indexOf(selectedDestination)

        TabRow(
            modifier = Modifier.onFocusChanged { isTabRowFocused = it.isFocused || it.hasFocus },
            selectedTabIndex = selectedDestinationIndex,
            indicator = { tabPositions, _ ->
                TopBarItemIndicator(
                    currentTabPosition = tabPositions[selectedDestinationIndex],
                    isTabRowFocused = isTabRowFocused
                )
            }
        ) {
            destinations.forEachIndexed { index, destination ->
                key(index) {
                    TabItem(
                        destination = destination,
                        selected = index == selectedDestinationIndex,
                        modifier = Modifier.height(32.dp),
                        onDestinationSelected = { onDestinationSelected(destination) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun TopBarItemIndicator(
    currentTabPosition: DpRect,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
    inactiveColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
    isTabRowFocused: Boolean
) {
    val width by animateDpAsState(targetValue = currentTabPosition.width, label = "width")
    val height = currentTabPosition.height
    val leftOffset by animateDpAsState(targetValue = currentTabPosition.left, label = "leftOffset")
    val topOffset = currentTabPosition.top
    val pillColor by animateColorAsState(targetValue = if (isTabRowFocused) activeColor else inactiveColor, label = "pillColor")

    Box(
        modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomStart)
            .offset(x = leftOffset, y = topOffset)
            .size(width = width, height = height)
            .background(color = pillColor)
            .zIndex(-1f)
    )
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun TabRowScope.TabItem(
    destination: HomeDestination,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onDestinationSelected: () -> Unit
) {
    Tab(
        modifier = modifier,
        selected = selected,
        onFocus = onDestinationSelected
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(horizontal = 16.dp),
            text = stringResource(destination.labelResId),
            style = MaterialTheme.typography.titleSmall
                .copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
        )
    }
}
