/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.media3.common.PlaybackException
import ch.srgssr.pillarbox.core.business.SRGErrorMessageProvider

/**
 * Player error view with a retry button.
 *
 * @param playerError The [PlaybackException] to display.
 * @param modifier The modifier to be applied to the layout.
 * @param sessionId The PlaybackMediaSession id.
 * @param onRetry The retry action.
 * @receiver
 */
@Composable
fun PlayerError(
    playerError: PlaybackException,
    modifier: Modifier = Modifier,
    sessionId: String? = null,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val errorMessageProvider = remember(context) {
        SRGErrorMessageProvider(context)
    }
    Surface(modifier, color = Color.Black) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onRetry() }
        ) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = errorMessageProvider.getErrorMessage(playerError).second,
                    color = Color.White
                )
                Text(text = "Tap to retry!", color = Color.LightGray, fontStyle = FontStyle.Italic)
                sessionId?.let {
                    Text(text = it, color = Color.LightGray, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}
