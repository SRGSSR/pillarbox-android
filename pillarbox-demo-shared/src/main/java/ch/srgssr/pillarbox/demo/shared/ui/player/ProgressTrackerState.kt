/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.exoplayer.image.ImageOutput
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.ImageProgressTrackerState
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.SimpleProgressTrackerState
import kotlinx.coroutines.CoroutineScope

/**
 * Creates a [ProgressTrackerState] to track manual changes made to the current media being player.
 *
 * @param player The [Player] to observe.
 * @param coroutineScope
 * @param imageOutput The [ImageOutput] to render the image track. Only applicable is smooth seeking is enabled in the app settings.
 */
@Composable
fun rememberProgressTrackerState(
    player: PillarboxPlayer,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    imageOutput: ImageOutput? = null,
): ProgressTrackerState {
    val context = LocalContext.current
    val appSettingsRepository = remember { AppSettingsRepository(context) }
    val appSettings by appSettingsRepository.getAppSettings().collectAsState(AppSettings())
    val smoothSeekingEnabled = appSettings.smoothSeekingEnabled

    return remember(player, smoothSeekingEnabled, imageOutput) {
        imageOutput?.let {
            ImageProgressTrackerState(player, coroutineScope, imageOutput)
        } ?: SimpleProgressTrackerState(player = player, coroutineScope = coroutineScope, useScrubbingMode = smoothSeekingEnabled)
    }
}
