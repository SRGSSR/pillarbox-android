/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(UPSIDE_DOWN_CAKE)
@Composable
internal fun AndroidSurfaceViewWithApi34WorkAround(player: Player?, modifier: Modifier = Modifier) {
    var view by remember { mutableStateOf<PlayerSurfaceView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { PlayerSurfaceView(it) },
        onReset = {},
        update = { view = it },
    )

    view?.let { view ->
        LaunchedEffect(view, player) {
            if (player != null) {
                view.player = player
            } else {
                // Now that our player got null'd, we are not in a rush to get the old view from the
                // previous player. Instead, we schedule clearing of the view for later on the main thread,
                // since that player might have a new view attached to it in the meantime. This will avoid
                // unnecessarily creating a Surface placeholder.
                withContext(Dispatchers.Main) {
                    view.player = null
                }
            }
        }
    }
}
