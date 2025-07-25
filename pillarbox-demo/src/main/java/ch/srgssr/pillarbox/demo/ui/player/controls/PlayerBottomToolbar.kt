/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material3.IconButton
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
 * @param shuffleEnabled Whether the shuffle mode is enabled.
 * @param onShuffleClick The action to perform when the shuffle button is clicked. `null` to hide the button.
 * @param repeatMode The repeat mode.
 * @param onRepeatClick The action to perform when the repeat button is clicked. `null` to hide the button.
 * @param pictureInPictureEnabled Whether picture in picture is enabled.
 * @param onPictureInPictureClick The action to perform when the picture in picture button is clicked. `null` to hide the button.
 * @param fullScreenEnabled Whether fullscreen is enabled.
 * @param onFullscreenClick The action to perform when the fullscreen button is clicked. `null` to hide the button.
 * @param onSettingsClick The action to perform when the settings button is clicked. `null` to hide the button.
 */
@Composable
fun PlayerBottomToolbar(
    modifier: Modifier = Modifier,
    shuffleEnabled: Boolean,
    onShuffleClick: (() -> Unit)?,
    repeatMode: @Player.RepeatMode Int,
    onRepeatClick: (() -> Unit)?,
    pictureInPictureEnabled: Boolean,
    onPictureInPictureClick: (() -> Unit)?,
    fullScreenEnabled: Boolean,
    onFullscreenClick: (() -> Unit)?,
    onSettingsClick: () -> Unit,
) {
    Row(modifier = modifier) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            ToggleableIconButton(
                checked = shuffleEnabled,
                icon = if (shuffleEnabled) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                contentDestination = stringResource(R.string.shuffle),
                onCheckedChange = onShuffleClick,
            )

            ToggleableIconButton(
                checked = repeatMode != Player.REPEAT_MODE_OFF,
                icon = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> Icons.Default.Repeat
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOneOn
                    Player.REPEAT_MODE_ALL -> Icons.Default.RepeatOn
                    else -> error("Unrecognized repeat mode $repeatMode")
                },
                contentDestination = stringResource(R.string.repeat_mode),
                onCheckedChange = onRepeatClick,
            )

            Spacer(modifier = Modifier.weight(1f))

            ToggleableIconButton(
                checked = pictureInPictureEnabled,
                icon = Icons.Default.PictureInPicture,
                contentDestination = stringResource(R.string.picture_in_picture),
                onCheckedChange = onPictureInPictureClick,
            )

            ToggleableIconButton(
                checked = fullScreenEnabled,
                icon = if (fullScreenEnabled) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDestination = stringResource(R.string.fullscreen),
                onCheckedChange = onFullscreenClick,
            )

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(sharedR.string.settings),
                )
            }
        }
    }
}

@Composable
private fun ToggleableIconButton(
    checked: Boolean,
    icon: ImageVector,
    contentDestination: String,
    onCheckedChange: (() -> Unit)?,
) {
    AnimatedVisibility(visible = onCheckedChange != null) {
        IconToggleButton(
            checked = checked,
            onCheckedChange = { onCheckedChange?.invoke() },
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDestination,
            )
        }
    }
}

@Preview
@Composable
private fun PlayerBottomToolbarPreview() {
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var pictureInPictureEnabled by remember { mutableStateOf(false) }
    var fullscreenEnabled by remember { mutableStateOf(false) }

    PillarboxTheme {
        Surface {
            PlayerBottomToolbar(
                modifier = Modifier.background(Color.Black),
                shuffleEnabled = shuffleEnabled,
                onShuffleClick = { shuffleEnabled = !shuffleEnabled },
                repeatMode = repeatMode,
                onRepeatClick = {
                    repeatMode = when (repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
                        else -> error("Unrecognized repeat mode $repeatMode")
                    }
                },
                pictureInPictureEnabled = pictureInPictureEnabled,
                onPictureInPictureClick = { pictureInPictureEnabled = !pictureInPictureEnabled },
                fullScreenEnabled = fullscreenEnabled,
                onFullscreenClick = { fullscreenEnabled = !fullscreenEnabled },
                onSettingsClick = {},
            )
        }
    }
}
