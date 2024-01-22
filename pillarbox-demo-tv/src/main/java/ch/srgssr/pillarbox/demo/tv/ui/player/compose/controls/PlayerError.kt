/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.PlaybackException
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.core.business.SRGErrorMessageProvider

/**
 * Player error
 *
 * @param playerError The player error.
 * @param modifier The modifier to layout the view.
 * @param onRetry Action to retry.
 * @receiver
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerError(playerError: PlaybackException, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    val context = LocalContext.current
    val errorMessageProvider = remember(context) {
        SRGErrorMessageProvider(context)
    }
    Column(
        modifier = modifier.clickable { onRetry() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessageProvider.getErrorMessage(playerError).second,
            color = Color.White
        )
        Text(text = "Click to retry!", color = Color.LightGray, fontStyle = FontStyle.Italic)
    }
}

@Preview
@Composable
private fun PlayerErrorPreview() {
    PlayerError(playerError = PlaybackException("", null, PlaybackException.ERROR_CODE_UNSPECIFIED)) {
        // Nothing
    }
}
