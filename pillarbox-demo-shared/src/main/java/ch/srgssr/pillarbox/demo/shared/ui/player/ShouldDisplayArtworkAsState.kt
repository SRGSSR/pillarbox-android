/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Whether the artwork should be displayed or not.
 */
@Composable
fun Player.shouldDisplayArtworkAsState(): State<Boolean> {
    val flow = remember { shouldDisplayArtworkAsFlow() }
    return flow.collectAsState(shouldDisplayArtwork())
}

private fun Player.shouldDisplayArtworkAsFlow(): Flow<Boolean> = callbackFlow {
    val listener = object : Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_TRACKS_CHANGED)) {
                trySend(shouldDisplayArtwork())
            }
            if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
                trySend(false)
            }
        }
    }
    addListener(listener)
    awaitClose {
        removeListener(listener)
    }
}

private fun Player.shouldDisplayArtwork(): Boolean {
    val hasVideoOrImage = currentTracks.isTypeSelected(C.TRACK_TYPE_VIDEO) || currentTracks.isTypeSelected(C.TRACK_TYPE_IMAGE)
    return deviceInfo.playbackType == DeviceInfo.PLAYBACK_TYPE_REMOTE || !hasVideoOrImage
}
