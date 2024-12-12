/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ch.srgssr.pillarbox.demo.shared.R

/**
 * Player bottom toolbar that contains Picture in Picture and fullscreen buttons.
 *
 * @param fullScreenEnabled if fullscreen is enabled
 * @param modifier The modifier to be applied to the layout.
 * @param fullScreenClicked action when fullscreen button is clicked
 * @param pictureInPictureClicked action when picture in picture is clicked
 * @param optionClicked action when settings is clicked
 */
@Composable
fun PlayerBottomToolbar(
    fullScreenEnabled: Boolean,
    modifier: Modifier = Modifier,
    fullScreenClicked: () -> Unit,
    pictureInPictureClicked: (() -> Unit)?,
    optionClicked: () -> Unit,
) {
    Row(modifier = modifier) {
        pictureInPictureClicked?.let {
            IconButton(onClick = it) {
                Icon(
                    tint = Color.White,
                    imageVector = Icons.Default.PictureInPicture,
                    contentDescription = "Picture in picture"
                )
            }
        }

        IconButton(onClick = fullScreenClicked) {
            if (fullScreenEnabled) {
                Icon(
                    tint = Color.White,
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit fullscreen"
                )
            } else {
                Icon(
                    tint = Color.White,
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Enter fullscreen"
                )
            }
        }

        IconButton(onClick = optionClicked) {
            Icon(
                tint = Color.White,
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings)
            )
        }
    }
}
