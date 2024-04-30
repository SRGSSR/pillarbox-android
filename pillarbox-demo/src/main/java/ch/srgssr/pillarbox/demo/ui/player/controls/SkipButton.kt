/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Skip button.
 *
 * @param modifier The [Modifier] to apply to the layout.
 * @param label The label to display on the button.
 * @param onClick The action to perform when clicking on the button.
 */
@Composable
fun SkipButton(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.skip),
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(text = label)
    }
}

@Composable
@PreviewLightDark
private fun SkipButtonPreview() {
    PillarboxTheme {
        SkipButton(
            onClick = {},
        )
    }
}
