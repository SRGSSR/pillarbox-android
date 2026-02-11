/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.network.HttpResultException
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.network.jsonSerializer
import ch.srgssr.pillarbox.standard.PlayerData
import ch.srgssr.pillarbox.standard.PlayerDataLoader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request

class PillarboxDemoService(
    private val okHttpClient: OkHttpClient = PillarboxOkHttp(),
    private val json: Json = jsonSerializer,
) : PlayerDataLoader<CustomData> {

    override fun canLoad(mediaItem: MediaItem): Boolean {
        return mediaItem.mediaId.startsWith("pillarbox:") && mediaItem.localConfiguration?.uri != null
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun load(mediaItem: MediaItem): Result<PlayerData<CustomData>> {
        val request = Request.Builder()
            .url(checkNotNull(mediaItem.localConfiguration).uri.toString())
            .build()
        return runCatching {
            okHttpClient.newCall(request)
                .execute()
                .use { response ->
                    if (response.isSuccessful) {
                        val bodyStream = checkNotNull(response.body).byteStream()
                        json.decodeFromStream(bodyStream)
                    } else {
                        throw HttpResultException(response.code, response.message)
                    }
                }
        }
    }
}
