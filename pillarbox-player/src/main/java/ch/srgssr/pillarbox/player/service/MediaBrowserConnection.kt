/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaBrowser
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
 * Media browser connection
 *
 * @param context
 * @param serviceComponent
 */
class MediaBrowserConnection(context: Context, serviceComponent: ComponentName) : MediaBrowser.Listener {
    private val _mediaBrowser = MutableStateFlow<MediaBrowser?>(null)

    /**
     * [MediaBrowser] as a StateFlow
     */
    val mediaBrowser: StateFlow<MediaBrowser?> = _mediaBrowser
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    init {
        scope.launch {
            val newMediaBrowser = MediaBrowser.Builder(context, SessionToken(context, serviceComponent))
                .setListener(this@MediaBrowserConnection)
                .buildAsync()
                .await()
            _mediaBrowser.value = newMediaBrowser
        }
    }

    /**
     * Release the [MediaBrowser]
     * Can't use this class anymore
     */
    fun release() {
        _mediaBrowser.value?.release()
        _mediaBrowser.value = null
    }

    override fun onDisconnected(controller: MediaController) {
        super.onDisconnected(controller)
        release()
    }
}
