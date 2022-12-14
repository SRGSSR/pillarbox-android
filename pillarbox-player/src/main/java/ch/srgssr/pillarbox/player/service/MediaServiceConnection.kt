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
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Media service connection base class to handle [MediaController] or [MediaBrowser] connection.
 *
 * @param T [MediaController] or [MediaBrowser]
 * @constructor
 *
 * @param context application context
 * @param serviceComponent declared service in the manifest
 */
abstract class MediaServiceConnection<T : MediaController>(context: Context, serviceComponent: ComponentName) : MediaController.Listener {
    private val _mediaController = MutableStateFlow<T?>(null)

    /**
     * [MediaController] as a StateFlow
     */
    val mediaController: StateFlow<T?> = _mediaController
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    init {
        scope.launch {
            val newController = build(context, SessionToken(context, serviceComponent))
            _mediaController.value = newController
        }
    }

    /**
     * Build a [MediaController] or [MediaBrowser]
     */
    protected abstract suspend fun build(context: Context, sessionToken: SessionToken): T

    /**
     * Release the [MediaController] or [MediaBrowser]
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
