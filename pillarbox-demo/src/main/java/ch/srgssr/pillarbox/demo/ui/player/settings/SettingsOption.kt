/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Settings option
 *
 * @param content
 * @param selected
 * @param onClick
 * @param modifier
 * @param enabled
 * @param overlineContent
 * @param supportingContent
 * @param trailingContent
 * @receiver
 * @receiver
 */
@Composable
fun SettingsOption(
    content: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val selectableModifier = Modifier.selectable(
        selected = selected,
        role = Role.RadioButton,
        enabled = enabled,
        onClick = onClick,
    )

    ListItem(
        modifier = modifier
            .then(Modifier.minimumInteractiveComponentSize())
            .then(selectableModifier),
        headlineContent = content,
        supportingContent = supportingContent,
        leadingContent = {
            RadioButton(selected = selected, enabled = enabled, onClick = null)
        },
        trailingContent = trailingContent,
        overlineContent = overlineContent,
    )
}

@Preview
@Composable
internal fun SettingsOptionPreview() {
    PillarboxTheme(darkTheme = true) {
        Column(Modifier.fillMaxWidth()) {
            SettingsOption(
                content = {
                    Text(text = "Options 1")
                },
                selected = false,
                onClick = { }
            )
            SettingsOption(
                content = {
                    Text(text = "Options 2")
                },
                trailingContent = {
                    Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null)
                },
                selected = true,
                onClick = { }
            )
            SettingsOption(
                content = {
                    Text(text = "Options 3")
                },
                supportingContent = { Text(text = "Option disabled") },
                selected = false,
                enabled = false,
                onClick = { }
            )
        }
    }
}
