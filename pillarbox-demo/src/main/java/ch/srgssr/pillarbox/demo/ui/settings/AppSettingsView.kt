/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaLibraryInfo
import ch.srgssr.pillarbox.demo.BuildConfig
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsViewModel
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * App settings view
 *
 * @param settingsViewModel The [AppSettingsViewModel]
 * @param modifier The [Modifier] to apply to the layout.
 */
@Composable
fun AppSettingsView(
    settingsViewModel: AppSettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val appSettings by settingsViewModel.currentAppSettings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(bottom = MaterialTheme.paddings.baseline)
            .verticalScroll(rememberScrollState()),
    ) {
        MetricsOverlaySettings(
            appSettings = appSettings,
            setMetricsOverlayTextSize = settingsViewModel::setMetricsOverlayTextSize,
            setMetricsOverlayEnabled = settingsViewModel::setMetricsOverlayEnabled,
            setMetricsOverlayTextColor = settingsViewModel::setMetricsOverlayTextColor,
        )

        LibraryVersionSection()
    }
}

@Composable
private fun MetricsOverlaySettings(
    appSettings: AppSettings,
    setMetricsOverlayEnabled: (Boolean) -> Unit,
    setMetricsOverlayTextColor: (AppSettings.TextColor) -> Unit,
    setMetricsOverlayTextSize: (AppSettings.TextSize) -> Unit,
) {
    SettingSection(title = stringResource(R.string.setting_metrics_overlay)) {
        TextLabel(text = stringResource(R.string.settings_enabled_overlay_description))

        LabeledSwitch(
            text = stringResource(R.string.settings_enabled_metrics_overlay),
            checked = appSettings.metricsOverlayEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.paddings.small),
            onCheckedChange = setMetricsOverlayEnabled,
        )

        AnimatedVisibility(visible = appSettings.metricsOverlayEnabled) {
            Column {
                DropdownSetting(
                    text = stringResource(R.string.settings_choose_text_color),
                    entries = AppSettings.TextColor.entries,
                    selectedEntry = appSettings.metricsOverlayTextColor,
                    modifier = Modifier.fillMaxWidth(),
                    onEntrySelected = setMetricsOverlayTextColor,
                )

                DropdownSetting(
                    text = stringResource(R.string.settings_choose_text_size),
                    entries = AppSettings.TextSize.entries,
                    selectedEntry = appSettings.metricsOverlayTextSize,
                    modifier = Modifier.fillMaxWidth(),
                    onEntrySelected = setMetricsOverlayTextSize,
                )
            }
        }
    }
}

@Composable
private fun LibraryVersionSection() {
    SettingSection(title = stringResource(R.string.settings_library_version)) {
        DemoListItemView(
            leadingText = "Pillarbox",
            trailingText = BuildConfig.VERSION_NAME,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        DemoListItemView(
            leadingText = "Media3",
            trailingText = MediaLibraryInfo.VERSION,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    DemoListHeaderView(
        title = title,
        modifier = Modifier.padding(start = MaterialTheme.paddings.baseline)
    )

    DemoListSectionView(content = content)
}

@Composable
private fun TextLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(
            horizontal = MaterialTheme.paddings.baseline,
            vertical = MaterialTheme.paddings.small
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun LabeledSwitch(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .clickable { onCheckedChange(!checked) }
            .minimumInteractiveComponentSize()
            .padding(end = MaterialTheme.paddings.baseline),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextLabel(text = text)

        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun <T> DropdownSetting(
    text: String,
    entries: List<T>,
    selectedEntry: T,
    modifier: Modifier = Modifier,
    onEntrySelected: (entry: T) -> Unit,
) {
    var dropdownOffset by remember { mutableStateOf(DpOffset.Zero) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            val pressInteraction = PressInteraction.Press(it)

                            interactionSource.emit(pressInteraction)

                            dropdownOffset = DpOffset(
                                x = it.x.toDp(),
                                y = (it.y - size.height).toDp(),
                            )
                            showDropdownMenu = true

                            if (tryAwaitRelease()) {
                                interactionSource.emit(PressInteraction.Release(pressInteraction))
                            } else {
                                interactionSource.emit(PressInteraction.Cancel(pressInteraction))
                            }
                        }
                    )
                }
                .indication(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                )
                .minimumInteractiveComponentSize()
                .padding(end = MaterialTheme.paddings.baseline),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextLabel(text = text)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconRotation by animateFloatAsState(
                    targetValue = if (showDropdownMenu) -180f else 0f,
                    label = "icon_rotation_animation"
                )

                Text(text = selectedEntry.toString())

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(iconRotation),
                )
            }
        }

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            offset = dropdownOffset,
        ) {
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(text = entry.toString()) },
                    onClick = {
                        onEntrySelected(entry)
                        showDropdownMenu = false
                    },
                    leadingIcon = {
                        AnimatedVisibility(entry == selectedEntry) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppSettingsPreview() {
    val appSettingsRepository = AppSettingsRepository(LocalContext.current)
    PillarboxTheme {
        AppSettingsView(AppSettingsViewModel(appSettingsRepository))
    }
}
