/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.standard.PlayerData
import ch.srgssr.pillarbox.standard.PlayerDataLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * [PlayerDataLoader] that downloads the [PlayerData] from the URL of the [MediaItem], following the Pillarbox standard metadata format.
 *
 * Every http(s) URL is loaded as metadata, so it should only be used by a player dedicated to such URLs.
 *
 * @param okHttpClient The [OkHttpClient] used to download the metadata.
 */
internal class UrlPlayerDataLoader(
    private val okHttpClient: OkHttpClient = PillarboxOkHttp(),
) : PlayerDataLoader<CustomData> {
    private val json = Json { ignoreUnknownKeys = true }

    override fun canLoad(mediaItem: MediaItem): Boolean {
        val scheme = mediaItem.localConfiguration?.uri?.scheme
        return scheme == "http" || scheme == "https"
    }

    override suspend fun load(mediaItem: MediaItem): Result<PlayerData<CustomData>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(checkNotNull(mediaItem.localConfiguration).uri.toString())
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code} ${response.message}")
                }
                json.decodeFromString<PlayerData<CustomData>>(response.body.string())
            }
        }
    }
}
