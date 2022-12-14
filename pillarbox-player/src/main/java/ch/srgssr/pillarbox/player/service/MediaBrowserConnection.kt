/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import kotlinx.coroutines.guava.await

/**
 * Media browser connection
 *
 * @param context
 * @param serviceComponent
 */
open class MediaBrowserConnection(context: Context, serviceComponent: ComponentName) :
    MediaServiceConnection<MediaBrowser>(context, serviceComponent),
    MediaBrowser.Listener {

    override suspend fun build(context: Context, sessionToken: SessionToken): MediaBrowser {
        return MediaBrowser.Builder(context, sessionToken)
            .setListener(this@MediaBrowserConnection)
            .buildAsync()
            .await()
    }
}
