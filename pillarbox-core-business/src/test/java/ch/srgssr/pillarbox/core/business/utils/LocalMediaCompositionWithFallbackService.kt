/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.utils

import android.content.Context
import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.player.network.jsonSerializer

internal class LocalMediaCompositionWithFallbackService(
    context: Context,
    private val fallbackService: MediaCompositionService = HttpMediaCompositionService(),
) : MediaCompositionService {
    private var mediaCompositions = mutableListOf<MediaComposition>()

    init {
        val json = context.assets.open("media-compositions.json").bufferedReader().use { it.readText() }
        mediaCompositions = jsonSerializer.decodeFromString(json)
    }

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
        val urn = uri.lastPathSegment
        val mediaComposition = mediaCompositions.firstOrNull { it.chapterUrn == urn }
        return if (mediaComposition != null) {
            Result.success(mediaComposition)
        } else {
            fallbackService.fetchMediaComposition(uri)
        }
    }

    companion object {
        const val URN_LIVE_DVR_VIDEO_RADIO = "urn:rts:video:8841634-radio"
        const val URN_LIVE_DVR_VIDEO_TV = "urn:rts:video:8841634-tv"
        const val URN_LIVE_DVR_AUDIO = "urn:rts:audio:3262363"
        const val URN_VOD = "urn:rts:video:8806923"

        /*
         * The urn that still used SRG url until we found a url with a short duration (10s or less).
         */
        const val URN_VOD_SHORT = "urn:rts:video:13444428"
        const val URN_AUDIO = "urn:rts:audio:13598743"
    }
}
