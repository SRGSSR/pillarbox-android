/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Player view when there is no content
 *
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlayerNoContent(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.then(
            Modifier.background(color = Color.Black)
        )
    ) {
        Text(
            color = Color.White,
            text = "No content",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PillarboxTheme {
        PlayerNoContent(modifier = Modifier.fillMaxSize())
    }
}
