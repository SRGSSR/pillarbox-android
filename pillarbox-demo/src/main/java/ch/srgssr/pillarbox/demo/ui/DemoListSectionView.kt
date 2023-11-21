/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Demo section view.
 *
 * @param modifier The [Modifier] to apply to the root of the section.
 * @param content The content of the section.
 */
@Composable
fun DemoListSectionView(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier) {
        Column {
            content()
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun DemoListSectionViewPreview() {
    PillarboxTheme {
        DemoListSectionView {
            Text(text = "Demo list section view")
        }
    }
}
