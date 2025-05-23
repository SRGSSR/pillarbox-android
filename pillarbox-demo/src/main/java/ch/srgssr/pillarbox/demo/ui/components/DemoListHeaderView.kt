/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Demo list header view.
 *
 * @param title The title of the header.
 * @param modifier The [Modifier] of the layout.
 * @param languageTag The IETF BCP47 language tag of the title.
 */
@Composable
fun DemoListHeaderView(
    title: String,
    modifier: Modifier = Modifier,
    languageTag: String? = null,
) {
    val localeList = languageTag?.let { LocaleList(Locale(languageTag)) }

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(localeList = localeList)) {
                append(title)
            }
        },
        modifier = modifier.padding(
            top = MaterialTheme.paddings.baseline,
            bottom = MaterialTheme.paddings.mini,
        ),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
@Preview(showBackground = true)
private fun DemoListHeaderViewPreview() {
    PillarboxTheme {
        DemoListHeaderView(title = "Demo list header")
    }
}
