/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.service

import ch.srg.pillarbox.core.business.integrationlayer.data.MediaComposition
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.URL

/**
 * Implementation of MediaCompositionDataSource using integration layer.
 *
 * @property mediaCompositionService
 */
class MediaCompositionDataSourceImpl(private val mediaCompositionService: MediaCompositionService) : MediaCompositionDataSource {
    constructor(host: URL, okHttpClient: OkHttpClient? = null) : this(createMediaCompositionService(host, okHttpClient))

    override suspend fun getMediaCompositionByUrn(urn: String): RemoteResult<MediaComposition> {
        return try {
            val result = mediaCompositionService.getMediaCompositionByUrn(urn)
            RemoteResult.Success(result)
        } catch (e: HttpException) {
            RemoteResult.Error(e, e.code())
        } catch (e: IOException) {
            RemoteResult.Error(e)
        }
    }

    companion object {

        private fun createMediaCompositionService(ilHost: URL, callFactory: okhttp3.Call.Factory? = null): MediaCompositionService {
            val builder = Retrofit.Builder()
                .baseUrl(ilHost)
                .addConverterFactory(GsonConverterFactory.create())
            callFactory?.let {
                builder.callFactory(it)
            }
            return builder.build().create(MediaCompositionService::class.java)
        }
    }
}
