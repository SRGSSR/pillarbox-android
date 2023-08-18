/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Settings option
 *
 * @param selected Settings selected.
 * @param onClick Click event.
 * @param modifier The Modifier to layout.
 * @param enabled Enabled.
 * @param content Content for the label.
 * @receiver
 * @receiver
 */
@Composable
fun SettingsOption(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val selectableModifier = Modifier.selectable(
        selected = selected,
        role = Role.RadioButton,
        enabled = enabled,
        onClick = onClick,
    )
    Row(
        modifier
            .then(Modifier.minimumInteractiveComponentSize())
            .then(selectableModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, enabled = enabled, onClick = null)
        content(this)
    }
}

@Preview
@Composable
internal fun SettingsOptionPreview() {
    PillarboxTheme() {
        Column(Modifier.fillMaxWidth()) {
            Switch(checked = true, onCheckedChange = {})
            SettingsOption(selected = false, onClick = { }) {
                Text(text = "Options 1")
            }
            SettingsOption(selected = true, onClick = { }) {
                Text(text = "Options 2")
                Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null)
            }
            SettingsOption(selected = false, enabled = false, onClick = { }) {
                Text(text = "Options 3")
            }
            SettingsOption(selected = true, enabled = false, onClick = { }) {
                Text(text = "Options 4")
            }
        }
    }
}
