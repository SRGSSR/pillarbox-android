/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaLibraryInfo
import ch.srgssr.pillarbox.cast.getCastContext
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
            .verticalScroll(rememberScrollState()),
    ) {
        MetricsOverlaySettings(
            appSettings = appSettings,
            setMetricsOverlayTextSize = settingsViewModel::setMetricsOverlayTextSize,
            setMetricsOverlayEnabled = settingsViewModel::setMetricsOverlayEnabled,
            setMetricsOverlayTextColor = settingsViewModel::setMetricsOverlayTextColor,
        )

        CastSettingsSection(
            appSettings = appSettings,
            setApplicationReceiverId = settingsViewModel::setReceiverApplicationId,
        )

        GitHubSection()

        VersionInformationSection()

        Spacer(Modifier.height(MaterialTheme.paddings.baseline))
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
        TextLabel(
            text = stringResource(R.string.settings_enabled_overlay_description),
            modifier = Modifier.padding(top = MaterialTheme.paddings.small),
        )

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
                    text = stringResource(R.string.settings_metrics_overlay_text_color),
                    entries = AppSettings.TextColor.entries,
                    selectedEntry = appSettings.metricsOverlayTextColor,
                    modifier = Modifier.fillMaxWidth(),
                    onEntrySelected = setMetricsOverlayTextColor,
                )

                DropdownSetting(
                    text = stringResource(R.string.settings_metrics_overlay_text_size),
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
private fun VersionInformationSection() {
    SettingSection(title = stringResource(R.string.settings_version_information)) {
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
private fun CastSettingsSection(
    appSettings: AppSettings,
    setApplicationReceiverId: (String) -> Unit,
) {
    SettingSection(title = stringResource(R.string.settings_cast)) {
        TextLabel(
            text = stringResource(R.string.settings_application_receiver_id_description),
            modifier = Modifier.padding(top = MaterialTheme.paddings.small),
        )
        var showCustomReceiverDialog by remember { mutableStateOf(false) }
        DropdownSetting(
            text = stringResource(R.string.settings_cast_receiver_id_source),
            entries = AppSettings.ReceiverType.entries,
            selectedEntry = appSettings.receiverType,
            modifier = Modifier.fillMaxWidth(),
            onEntrySelected = {
                when (it) {
                    AppSettings.ReceiverType.Letterbox -> setApplicationReceiverId(AppSettings.ReceiverId.Letterbox)
                    AppSettings.ReceiverType.Google -> setApplicationReceiverId(AppSettings.ReceiverId.Google)
                    AppSettings.ReceiverType.Media3 -> setApplicationReceiverId(AppSettings.ReceiverId.Media3)
                    else -> showCustomReceiverDialog = true
                }
            },
        )
        if (showCustomReceiverDialog) {
            CustomReceiverDialog(onDismiss = {
                showCustomReceiverDialog = false
            }, onConfirm = {
                setApplicationReceiverId(it)
                showCustomReceiverDialog = false
            })
        }
        DemoListItemView(
            leadingText = stringResource(R.string.settings_application_receiver_id),
            trailingText = appSettings.receiverApplicationId,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Suppress("TooGenericExceptionCaught")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomReceiverDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val castContext = LocalContext.current.getCastContext()
    val focusRequester = remember { FocusRequester() }
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.paddings.baseline)) {
                Text(
                    text = stringResource(R.string.settings_custom_cast_receiver),
                    modifier = Modifier.padding(MaterialTheme.paddings.baseline),
                    color = AlertDialogDefaults.titleContentColor,
                    style = MaterialTheme.typography.headlineSmall,
                )

                var text by remember { mutableStateOf("") }
                var invalidIdError: Throwable? by remember { mutableStateOf(null) }
                val isValidText = text.length >= MinReceiverIdLength
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.focusRequester(focusRequester),
                    isError = !isValidText || invalidIdError != null,
                    singleLine = true,
                    label = {
                        Text(text = stringResource(R.string.settings_application_receiver_id))
                    },
                    supportingText = {
                        invalidIdError?.let {
                            Text(text = stringResource(R.string.settings_invalid_application_receiver_id))
                        }
                    }
                )

                Row {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                    TextButton(
                        enabled = isValidText,
                        onClick = {
                            try {
                                castContext.setReceiverApplicationId(text)
                                onConfirm(text)
                            } catch (e: Exception) {
                                invalidIdError = e
                            }
                        }
                    ) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun GitHubSection() {
    SettingSection(title = stringResource(R.string.settings_github)) {
        val uriHandler = LocalUriHandler.current

        DemoListItemView(
            title = stringResource(R.string.settings_github_source_code),
            modifier = Modifier.fillMaxWidth(),
            onClick = { uriHandler.openUri("https://github.com/SRGSSR/pillarbox-android") },
        )

        HorizontalDivider()

        DemoListItemView(
            title = stringResource(R.string.settings_github_project),
            modifier = Modifier.fillMaxWidth(),
            onClick = { uriHandler.openUri("https://github.com/orgs/SRGSSR/projects/9") },
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
            .toggleable(checked, onValueChange = onCheckedChange)
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
                .semantics(mergeDescendants = true) {
                    role = Role.DropdownList
                    onClick(text) {
                        showDropdownMenu = true
                        true
                    }
                }
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
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconScaleY by animateFloatAsState(
                    targetValue = if (showDropdownMenu) -1f else 1f,
                    label = "icon_scale_animation",
                )

                Text(text = selectedEntry.toString())

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.scale(scaleX = 1f, scaleY = iconScaleY),
                )
            }
        }

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            modifier = Modifier.semantics {
                collectionInfo = CollectionInfo(rowCount = entries.size, columnCount = 1)
            },
            offset = dropdownOffset,
        ) {
            entries.forEachIndexed { index, entry ->
                val isSelected = entry == selectedEntry

                DropdownMenuItem(
                    text = { Text(text = entry.toString()) },
                    onClick = {
                        onEntrySelected(entry)
                        showDropdownMenu = false
                    },
                    modifier = Modifier.semantics {
                        selected = isSelected
                        collectionItemInfo = CollectionItemInfo(
                            rowIndex = index,
                            rowSpan = 1,
                            columnIndex = 1,
                            columnSpan = 1,
                        )
                    },
                    leadingIcon = {
                        AnimatedVisibility(isSelected) {
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
private fun AppSettingsPreview() {
    val appSettingsRepository = AppSettingsRepository(LocalContext.current)
    val appSettingsViewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.Factory(appSettingsRepository))

    PillarboxTheme {
        AppSettingsView(appSettingsViewModel)
    }
}

private const val MinReceiverIdLength = 8
