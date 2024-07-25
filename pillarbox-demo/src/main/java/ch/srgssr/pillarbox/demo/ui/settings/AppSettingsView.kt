/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaLibraryInfo
import ch.srgssr.pillarbox.demo.BuildConfig
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsViewModel
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * App settings view
 *
 * @param settingsViewModel The [AppSettingsViewModel]
 */
@Composable
fun AppSettingsView(settingsViewModel: AppSettingsViewModel) {
    val appSettings by settingsViewModel.currentAppSettings.collectAsStateWithLifecycle(AppSettings())
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(MaterialTheme.paddings.small)
            .verticalScroll(state = scrollState, enabled = true),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
    ) {
        MetricsOverlay(
            modifier = Modifier.fillMaxWidth(),
            appSettings = appSettings,
            setMetricsOverlayTextSize = settingsViewModel::setMetricsOverlayTextSize,
            setMetricsOverlayEnabled = settingsViewModel::setMetricsOverlayEnabled,
            setMetricsOverlayTextColor = settingsViewModel::setMetricsOverlayTextColor
        )
        SettingsDivider()
        Text(
            text = stringResource(R.string.settings_library_version),
            style = MaterialTheme.typography.headlineMedium
        )
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)) {
            Text(
                text = "Pillarbox: ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Media3: ${MediaLibraryInfo.VERSION}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MetricsOverlay(
    appSettings: AppSettings,
    setMetricsOverlayEnabled: (Boolean) -> Unit,
    setMetricsOverlayTextColor: (AppSettings.TextColor) -> Unit,
    setMetricsOverlayTextSize: (AppSettings.TextSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)
    ) {
        Text(text = stringResource(R.string.setting_metrics_overlay), style = MaterialTheme.typography.headlineMedium)
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = stringResource(ch.srgssr.pillarbox.demo.shared.R.string.settings_enabled_overlay_description)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(ch.srgssr.pillarbox.demo.shared.R.string.settings_enabled_metrics_overlay),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = appSettings.metricsOverlayEnabled,
                onCheckedChange = setMetricsOverlayEnabled
            )
        }

        Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            val selected = appSettings.metricsOverlayTextColor
            val entries = AppSettings.TextColor.entries
            val labels = remember(entries) {
                entries.map { it.name }
            }
            Text(stringResource(R.string.settings_choose_text_color))
            DropDownSettings(
                modifier = Modifier,
                labels = labels,
                selectedEntry = selected,
                entries = entries,
                onEntryClicked = setMetricsOverlayTextColor
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val selected = appSettings.metricsOverlayTextSize
            val entries = AppSettings.TextSize.entries
            val labels = remember(entries) {
                entries.map { it.name }
            }
            Text(stringResource(R.string.settings_choose_text_size))
            DropDownSettings(
                modifier = Modifier,
                labels = labels,
                selectedEntry = selected,
                entries = entries,
                onEntryClicked = setMetricsOverlayTextSize
            )
        }
    }
}

@Composable
private fun <T> DropDownSettings(
    selectedEntry: T,
    entries: List<T>,
    labels: List<String>,
    onEntryClicked: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDropDownMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showDropDownMenu = true
                }
                .padding(
                    start = MaterialTheme.paddings.baseline,
                    end = MaterialTheme.paddings.small
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconRotation by animateFloatAsState(
                targetValue = if (showDropDownMenu) -180f else 0f,
                label = "icon_rotation_animation"
            )
            Text(text = labels[entries.indexOf(selectedEntry)])

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.rotate(iconRotation)
            )
        }

        DropdownMenu(
            expanded = showDropDownMenu,
            onDismissRequest = { showDropDownMenu = false },
            offset = DpOffset(
                x = 0.dp,
                y = 0.dp,
            )
        ) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = { Text(text = labels[index]) },
                    onClick = {
                        onEntryClicked(entry)
                        showDropDownMenu = false
                    },
                    trailingIcon = if (selectedEntry == entry) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = MaterialTheme.paddings.baseline),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Preview(showBackground = true)
@Composable
fun AppSettingsPreview() {
    val appSettingsRepository = AppSettingsRepository(LocalContext.current)
    PillarboxTheme {
        AppSettingsView(AppSettingsViewModel(appSettingsRepository))
    }
}

@Preview(showBackground = true)
@Composable
fun DropDownMenu() {
    PillarboxTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            val entries = listOf("Value 1", "Value 2", "Value 3")
            DropDownSettings(
                modifier = Modifier,
                selectedEntry = entries[0],
                entries = entries,
                labels = entries,
                onEntryClicked = {}
            )
        }
    }
}
