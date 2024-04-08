/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Display a live indicator
 *
 * @param isAtLive display the indicator in red if it is true.
 * @param modifier The modifier.
 * @param onClick The click event when [isAtLive] is false.
 * @receiver
 */
@Composable
fun LiveIndicator(
    isAtLive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.clickable(enabled = !isAtLive, onClick = onClick, role = Role.Button, onClickLabel = "back to live"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.drawBehind {
                this.drawRoundRect(color = if (isAtLive) Color.Red else Color.Gray, cornerRadius = CornerRadius(10f))
            }.wrapContentHeight(align = Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            text = " LIVE ",
            color = Color.White
        )
    }
}

@Preview(showBackground = false)
@Composable
private fun LiveIndicatorPreview() {
    MaterialTheme {
        Column(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiveIndicator(isAtLive = true)
            LiveIndicator(
                isAtLive = false
            )
        }
    }
}
