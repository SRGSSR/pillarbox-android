/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.RemoteResult
import kotlinx.serialization.json.Json

class LocalMediaCompositionDataSource(context: Context) : MediaCompositionDataSource {
    private val localData = HashMap<String, MediaComposition>()

    init {
        val jonsSerializer = Json { ignoreUnknownKeys = true }
        for (urn in urns) {
            val json = context.assets.open("$urn.json").bufferedReader().use { it.readText() }
            localData[urn] = jonsSerializer.decodeFromString(json)
        }
    }

    override suspend fun getMediaCompositionByUrn(urn: String): RemoteResult<MediaComposition> {
        return localData[urn]?.let {
            RemoteResult.Success(it)
        } ?: RemoteResult.Error(throwable = IllegalArgumentException("$urn not found!"))
    }

    companion object {
        const val Live = "urn:rts:video:8841634"
        const val Dvr = "urn:rts:audio:3262363"

        /**
         * Vod, ~ 11 min 52 seconds
         */
        const val Vod = "urn:srf:video:f10ba470-6a3c-4479-8b2a-4529f7066234"

        /**
         * Vod short, ~ 10 seconds
         */
        const val VodShort = "urn:rts:video:13444428"

        private val urns = arrayOf(Live, Dvr, Vod, VodShort)
    }

}
