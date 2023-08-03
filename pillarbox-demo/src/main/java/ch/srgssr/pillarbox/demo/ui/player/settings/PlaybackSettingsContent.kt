/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player

/**
 * Playback settings content
 *
 * @param player The [Player] actions occurred.
 * @param isVisible If the settings are visible.
 * @param onDismiss The callback to dismiss the settings.
 */
@Composable
fun ColumnScope.PlaybackSettingsContent(
    player: Player,
    isVisible: Boolean = false,
    onDismiss: () -> Unit,
) {
    val onDismissState = remember {
        onDismiss
    }
    val playerSettingsViewModel: PlayerSettingsViewModel = viewModel(factory = PlayerSettingsViewModel.Factory(player))
    val currentPlaybackSpeed = playerSettingsViewModel.currentPlaybackRateFlow.collectAsState()
    val stateUI = playerSettingsViewModel.uiState.collectAsState()
    LaunchedEffect(player, isVisible) {
        playerSettingsViewModel.openHome()
    }
    when (stateUI.value) {
        is PlayerSettingsUiState.Home -> {
            SettingsItem(
                modifier = Modifier.clickable(enabled = true, role = Role.Button) {
                    playerSettingsViewModel.openPlaybackRate()
                },
                title = "Speed", secondaryText = DefaultSpeedLabelProvider(currentPlaybackSpeed.value), imageVector = Icons.Default.Speed
            )
        }

        is PlayerSettingsUiState.PlaybackRate -> {
            PlaybackSpeedSettings(currentSpeed = currentPlaybackSpeed.value, onSpeedSelected = {
                playerSettingsViewModel.selectPlaybackRate(it)
                onDismissState()
            })
        }

        is PlayerSettingsUiState.TextTracks -> {
            Text(text = "TODO")
            onDismissState()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SettingsItem(
    title: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    secondaryText: String? = null
) {
    ListItem(
        modifier = modifier,
        icon = { Icon(imageVector = imageVector, contentDescription = null) },
        secondaryText = secondaryText?.let {
            { Text(text = it) }
        }
    ) {
        Text(text = title)
    }
}
