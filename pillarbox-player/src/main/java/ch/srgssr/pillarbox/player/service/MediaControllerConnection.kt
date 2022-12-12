/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Handle connection to [MediaController]
 *
 * @param context
 * @param serviceComponent
 */
class MediaControllerConnection(context: Context, serviceComponent: ComponentName) : MediaController.Listener {
    private val _mediaController = MutableStateFlow<MediaController?>(null)

    /**
     * [MediaController] as a StateFlow
     */
    val mediaController: StateFlow<MediaController?> = _mediaController
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    init {
        scope.launch {
            val newMediaController = MediaController.Builder(context, SessionToken(context, serviceComponent))
                .setListener(this@MediaControllerConnection)
                .buildAsync()
                .await()
            _mediaController.value = newMediaController
        }
    }

    /**
     * Release the [MediaController]
     * Can't use this class anymore
     */
    fun release() {
        _mediaController.value?.release()
        _mediaController.value = null
    }

    override fun onDisconnected(controller: MediaController) {
        super.onDisconnected(controller)
        release()
    }
}
