/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import android.app.Activity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.shared.R as sharedR

/**
 * Player bottom toolbar that contains Picture in Picture and fullscreen buttons.
 *
 * @param modifier The [Modifier] to apply to the layout.
 * @param isShuffleEnabled Whether shuffle is enabled.
 * @param isShuffleOn Whether shuffle is on.
 * @param onShuffleClick The shuffle button action.
 * @param isRepeatEnabled Whether repeat is enabled.
 * @param repeatMode The repeat mode.
 * @param onRepeatClick The repeat button action.
 * @param isPictureInPictureEnabled Whether Picture-in-Picture is enabled.
 * @param isInPictureInPicture Whether the [Activity] is currently in Picture-in-Picture mode.
 * @param onPictureInPictureClick The Picture-in-Picture button action.
 * @param isInFullscreen Whether fullscreen is enabled.
 * @param onFullscreenClick The fullscreen button action.
 * @param onSettingsClick The action to perform when the settings button is clicked. `null` to hide the button.
 */
@Composable
fun PlayerBottomToolbar(
    modifier: Modifier = Modifier,
    isShuffleEnabled: Boolean,
    isShuffleOn: Boolean,
    onShuffleClick: () -> Unit,
    isRepeatEnabled: Boolean,
    repeatMode: @Player.RepeatMode Int,
    onRepeatClick: () -> Unit,
    isPictureInPictureEnabled: Boolean,
    isInPictureInPicture: Boolean,
    onPictureInPictureClick: () -> Unit,
    isInFullscreen: Boolean,
    onFullscreenClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(modifier = modifier) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            ShuffleButton(
                enabled = isShuffleEnabled,
                isShuffleOn = isShuffleOn,
                onClick = onShuffleClick,
            )

            RepeatButton(
                enabled = isRepeatEnabled,
                repeatMode = repeatMode,
                onClick = onRepeatClick,
            )

            Spacer(modifier = Modifier.weight(1f))

            PictureInPictureButton(
                enabled = isPictureInPictureEnabled,
                isInPictureInPicture = isInPictureInPicture,
                onClick = onPictureInPictureClick,
            )

            FullscreenButton(
                isInFullscreen = isInFullscreen,
                onClick = onFullscreenClick,
            )

            SettingsButton(onClick = onSettingsClick)
        }
    }
}

@Composable
private fun ShuffleButton(
    enabled: Boolean,
    isShuffleOn: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconToggleButton(
        checked = isShuffleOn,
        enabled = enabled,
        imageVector = if (isShuffleOn) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
        contentDescription = if (isShuffleOn) stringResource(sharedR.string.shuffle_button_on) else stringResource(sharedR.string.shuffle_button_off),
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun RepeatButton(
    enabled: Boolean,
    repeatMode: @Player.RepeatMode Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconToggleButton(
        checked = repeatMode != Player.REPEAT_MODE_OFF,
        enabled = enabled,
        imageVector = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Icons.Default.Repeat
            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOneOn
            else -> Icons.Default.RepeatOn
        },
        contentDescription = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> stringResource(sharedR.string.repeat_button_off)
            Player.REPEAT_MODE_ONE -> stringResource(sharedR.string.repeat_button_one)
            else -> stringResource(sharedR.string.repeat_button_all)
        },
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun PictureInPictureButton(
    enabled: Boolean,
    isInPictureInPicture: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconToggleButton(
        checked = isInPictureInPicture,
        enabled = enabled,
        imageVector = Icons.Default.PictureInPicture,
        contentDescription = stringResource(R.string.picture_in_picture),
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun FullscreenButton(
    isInFullscreen: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconToggleButton(
        checked = isInFullscreen,
        enabled = true,
        imageVector = if (isInFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
        contentDescription = stringResource(R.string.fullscreen),
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun SettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconToggleButton(
        checked = false,
        enabled = true,
        imageVector = Icons.Default.Settings,
        contentDescription = stringResource(sharedR.string.settings),
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun IconToggleButton(
    checked: Boolean,
    enabled: Boolean,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onClick() },
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
        )
    }
}

@Preview
@Composable
private fun PlayerBottomToolbarPreview() {
    var isShuffleOn by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var isInPictureInPicture by remember { mutableStateOf(false) }
    var isInFullscreen by remember { mutableStateOf(false) }

    PillarboxTheme {
        Surface(color = Color.Black) {
            PlayerBottomToolbar(
                isShuffleEnabled = true,
                isShuffleOn = isShuffleOn,
                onShuffleClick = { isShuffleOn = !isShuffleOn },
                isRepeatEnabled = true,
                repeatMode = repeatMode,
                onRepeatClick = {
                    repeatMode = when (repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                        else -> Player.REPEAT_MODE_OFF
                    }
                },
                isPictureInPictureEnabled = true,
                isInPictureInPicture = isInPictureInPicture,
                onPictureInPictureClick = { isInPictureInPicture = !isInPictureInPicture },
                isInFullscreen = isInFullscreen,
                onFullscreenClick = { isInFullscreen = !isInFullscreen },
                onSettingsClick = {},
            )
        }
    }
}
