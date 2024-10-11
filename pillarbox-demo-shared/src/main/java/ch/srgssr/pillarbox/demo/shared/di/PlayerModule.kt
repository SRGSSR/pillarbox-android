/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.di

import android.content.Context
import ch.srg.dataProvider.integrationlayer.dependencies.modules.IlServiceModule
import ch.srg.dataProvider.integrationlayer.dependencies.modules.OkHttpModule
import ch.srgssr.dataprovider.paging.DataProviderPaging
import ch.srgssr.pillarbox.core.business.DefaultPillarbox.defaultMonitoringMessageHandler
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.source.BlockedTimeRangeAssetLoader
import ch.srgssr.pillarbox.demo.shared.source.CustomAssetLoader
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URL
import ch.srg.dataProvider.integrationlayer.request.IlHost as DataProviderIlHost

/**
 * Dependencies to make custom Dependency Injection
 */
object PlayerModule {
    /**
     * Provide default player that allow to play urls and urns content from the SRG
     */
    fun provideDefaultPlayer(context: Context): PillarboxExoPlayer {
        return PillarboxExoPlayer(
            context = context,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(SRGAssetLoader(context))
                addAssetLoader(CustomAssetLoader(context))
                addAssetLoader(BlockedTimeRangeAssetLoader(context))
            },
            monitoringMessageHandler = defaultMonitoringMessageHandler,
        )
    }

    /**
     * Create il repository
     */
    fun createIlRepository(
        context: Context,
        ilHost: URL = IlHost.DEFAULT,
        forceSAM: Boolean = false,
        ilLocation: String? = null,
    ): ILRepository {
        val okHttp = OkHttpModule.createOkHttpClient(context)
            .newBuilder()
            .addInterceptor(SamInterceptor(forceSAM))
            .addInterceptor(LocationInterceptor(ilLocation))
            .build()
        val ilService = IlServiceModule.createIlService(okHttp, ilHost = ilHost.toDataProviderIlHost(forceSAM))
        return ILRepository(dataProviderPaging = DataProviderPaging(ilService), ilService = ilService)
    }

    private fun URL.toDataProviderIlHost(forceSAM: Boolean): DataProviderIlHost {
        return when (this) {
            IlHost.PROD -> if (forceSAM) DataProviderIlHost.PROD_SAM else DataProviderIlHost.PROD
            IlHost.STAGE -> if (forceSAM) DataProviderIlHost.STAGE_SAM else DataProviderIlHost.STAGE
            IlHost.TEST -> if (forceSAM) DataProviderIlHost.TEST_SAM else DataProviderIlHost.TEST
            else -> if (forceSAM) DataProviderIlHost.PROD_SAM else DataProviderIlHost.PROD
        }
    }

    private class SamInterceptor(private val forceSAM: Boolean) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (!forceSAM) {
                return chain.proceed(request)
            }

            val newUrl = request.url
                .newBuilder()
                .addQueryParameter("forceSAM", "true")
                .build()
            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

            return chain.proceed(newRequest)
        }
    }

    private class LocationInterceptor(private val location: String?) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (location.isNullOrBlank()) {
                return chain.proceed(request)
            }

            val newUrl = request.url
                .newBuilder()
                .addQueryParameter("forceLocation", location)
                .build()
            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

            return chain.proceed(newRequest)
        }
    }
}
