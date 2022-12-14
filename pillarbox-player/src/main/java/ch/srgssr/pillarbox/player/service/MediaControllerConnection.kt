/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.guava.await

/**
 * Handle connection to [MediaController]
 *
 * @param context
 * @param serviceComponent
 */
open class MediaControllerConnection(context: Context, serviceComponent: ComponentName) :
    MediaServiceConnection<MediaController>(context, serviceComponent) {

    override suspend fun build(context: Context, sessionToken: SessionToken): MediaController {
        return MediaController.Builder(context, sessionToken)
            .setListener(this@MediaControllerConnection)
            .buildAsync()
            .await()
    }
}
