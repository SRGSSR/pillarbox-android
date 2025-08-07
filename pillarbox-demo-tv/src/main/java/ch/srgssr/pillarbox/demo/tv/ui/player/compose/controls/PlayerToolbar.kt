/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberRepeatButtonState
import androidx.media3.ui.compose.state.rememberShuffleButtonState
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.demo.shared.R as sharedR

@Composable
internal fun PlayerToolbar(
    player: Player,
    currentCredit: Credit?,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    onPlaylistClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
    ) {
        val iconButtonColors = IconButtonDefaults.colors()
        val activeIconButtonColors = IconButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        SettingsButton(onClick = onSettingsClick)

        PlaylistButton(onClick = onPlaylistClick)

        ShuffleButton(
            player = player,
            iconButtonColors = iconButtonColors,
            activeIconButtonColors = activeIconButtonColors,
        )

        RepeatButton(
            player = player,
            iconButtonColors = iconButtonColors,
            activeIconButtonColors = activeIconButtonColors,
        )

        if (currentCredit != null) {
            Spacer(modifier = Modifier.weight(1f))

            SkipButton(
                onClick = { player.seekTo(currentCredit.end) },
            )
        }
    }
}

@Composable
private fun SettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(sharedR.string.settings),
        )
    }
}

@Composable
private fun PlaylistButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
            contentDescription = stringResource(R.string.playlist),
        )
    }
}

@Composable
private fun ShuffleButton(
    player: Player,
    modifier: Modifier = Modifier,
    iconButtonColors: ButtonColors,
    activeIconButtonColors: ButtonColors,
) {
    val shuffleButtonState = rememberShuffleButtonState(player)

    IconButton(
        onClick = shuffleButtonState::onClick,
        modifier = modifier,
        enabled = shuffleButtonState.isEnabled,
        colors = if (shuffleButtonState.shuffleOn) activeIconButtonColors else iconButtonColors,
    ) {
        val contentDescription =
            if (shuffleButtonState.shuffleOn) stringResource(sharedR.string.shuffle_button_on) else stringResource(sharedR.string.shuffle_button_off)

        Icon(
            imageVector = Icons.Default.Shuffle,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun RepeatButton(
    player: Player,
    modifier: Modifier = Modifier,
    iconButtonColors: ButtonColors,
    activeIconButtonColors: ButtonColors,
) {
    val repeatButtonState = rememberRepeatButtonState(player)

    IconButton(
        onClick = repeatButtonState::onClick,
        modifier = modifier,
        enabled = repeatButtonState.isEnabled,
        colors = if (repeatButtonState.repeatModeState != Player.REPEAT_MODE_OFF) activeIconButtonColors else iconButtonColors,
    ) {
        val imageVector = if (repeatButtonState.repeatModeState == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
        val contentDescription = when (repeatButtonState.repeatModeState) {
            Player.REPEAT_MODE_OFF -> stringResource(sharedR.string.repeat_button_off)
            Player.REPEAT_MODE_ONE -> stringResource(sharedR.string.repeat_button_one)
            else -> stringResource(sharedR.string.repeat_button_all)
        }

        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
        )
    }
}
