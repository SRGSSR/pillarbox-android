/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Display a live indicator.
 *
 * @param isAtLive Display the indicator in red if it is `true`, in grey otherwise.
 * @param modifier The [Modifier] to apply to the layout.
 * @param onClick The click event when [isAtLive] is `false`.
 * @receiver
 */
@Composable
fun LiveIndicator(
    isAtLive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isAtLive) Color.Red else Color.Gray,
        label = "live_background_color",
    )

    Box(
        modifier = modifier
            .clickable(
                enabled = !isAtLive,
                onClick = onClick,
                role = Role.Button,
                onClickLabel = "Back to live",
            )
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small,
            ),
    ) {
        Text(
            text = "LIVE",
            modifier = Modifier.padding(horizontal = MaterialTheme.paddings.small),
            color = Color.White,
        )
    }
}

@Preview(showBackground = false)
@Composable
private fun LiveIndicatorPreview() {
    PillarboxTheme {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)) {
            LiveIndicator(isAtLive = true)

            LiveIndicator(isAtLive = false)
        }
    }
}
