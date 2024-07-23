/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaLibraryInfo
import ch.srgssr.pillarbox.demo.BuildConfig
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * App settings view
 *
 * @param appSettingsRepository The [AppSettingsRepository]
 */
@Composable
fun AppSettingsView(appSettingsRepository: AppSettingsRepository) {
    val appSettings by appSettingsRepository.getAppSettings().collectAsStateWithLifecycle(AppSettings())
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(MaterialTheme.paddings.small)) {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)) {
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
                    onCheckedChange = {
                        coroutineScope.launch(Dispatchers.IO) {
                            appSettingsRepository.setMetricsOverlayEnabled(it)
                        }
                    }
                )
            }
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = stringResource(ch.srgssr.pillarbox.demo.shared.R.string.settings_enabled_overlay_description)
            )
        }

        SettingsDivider()
        Text(
            text = stringResource(R.string.settings_library_version),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(MaterialTheme.paddings.small))
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
        AppSettingsView(appSettingsRepository)
    }
}
