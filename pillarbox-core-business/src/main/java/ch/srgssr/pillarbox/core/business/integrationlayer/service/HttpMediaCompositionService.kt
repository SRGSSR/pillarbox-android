/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.player.network.RequestSender.send
import okhttp3.Request

/**
 * A service for fetching a [MediaComposition] over HTTP.
 */
class HttpMediaCompositionService : MediaCompositionService {

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
        return Request.Builder()
            .url(uri.toString())
            .build()
            .send()
    }
}
