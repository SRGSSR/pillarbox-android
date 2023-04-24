/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Player bottom toolbar that contains Picture in Picture and fullscreen buttons.
 *
 * @param fullScreenEnabled if fullscreen is enabled
 * @param fullScreenClicked action when fullscreen button is clicked
 * @param pictureInPictureClicked action when picture in picture is clicked
 * @param modifier The Modifier to apply to.
 * @receiver
 * @receiver
 */
@Composable
fun PlayerBottomToolbar(
    fullScreenEnabled: Boolean,
    fullScreenClicked: (Boolean) -> Unit,
    pictureInPictureClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        IconButton(onClick = pictureInPictureClicked) {
            Icon(
                tint = Color.White,
                imageVector = Icons.Default.PictureInPicture, contentDescription = "Picture in picture"
            )
        }
        IconToggleButton(checked = fullScreenEnabled, onCheckedChange = fullScreenClicked) {
            if (fullScreenEnabled) {
                Icon(
                    tint = Color.White,
                    imageVector = Icons.Default.FullscreenExit, contentDescription = "Exit full screen"
                )
            } else {
                Icon(
                    tint = Color.White,
                    imageVector = Icons.Default.Fullscreen, contentDescription = "Open in full screen"
                )
            }
        }
    }
}
