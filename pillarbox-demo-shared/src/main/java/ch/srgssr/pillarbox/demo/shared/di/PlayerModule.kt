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

/**
 * Dependencies to make custom Dependency Injection
 */
object PlayerModule {
    /**
     * Prod host url
     */
    val IlHost.SAM_PROD: URL
        get() = URL("https://il.srgssr.ch/sam/")

    /**
     * Test host url
     */
    val IlHost.SAM_TEST: URL
        get() = URL("https://il-test.srgssr.ch/sam/")

    /**
     * Stage host url
     */
    val IlHost.SAM_STAGE: URL
        get() = URL("https://il-stage.srgssr.ch/sam/")

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
        ilLocation: String? = null,
    ): ILRepository {
        val okHttp = OkHttpModule.createOkHttpClient(context)
            .newBuilder()
            .addInterceptor(SamInterceptor(ilHost))
            .addInterceptor(LocationInterceptor(ilLocation))
            .build()
        val ilService = IlServiceModule.createIlService(okHttp, ilHost = providerIlHostFromUrl(ilHost))
        return ILRepository(dataProviderPaging = DataProviderPaging(ilService), ilService = ilService)
    }

    private fun providerIlHostFromUrl(ilHost: URL): ch.srg.dataProvider.integrationlayer.request.IlHost {
        return when (ilHost) {
            IlHost.PROD -> ch.srg.dataProvider.integrationlayer.request.IlHost.PROD
            IlHost.STAGE -> ch.srg.dataProvider.integrationlayer.request.IlHost.STAGE
            IlHost.TEST -> ch.srg.dataProvider.integrationlayer.request.IlHost.TEST
            IlHost.SAM_PROD -> ch.srg.dataProvider.integrationlayer.request.IlHost.PROD_SAM
            IlHost.SAM_STAGE -> ch.srg.dataProvider.integrationlayer.request.IlHost.STAGE_SAM
            IlHost.SAM_TEST -> ch.srg.dataProvider.integrationlayer.request.IlHost.TEST_SAM
            else -> ch.srg.dataProvider.integrationlayer.request.IlHost.PROD
        }
    }

    private class SamInterceptor(ilHost: URL) : Interceptor {
        private val ilHost: String = ilHost.toString()

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (!isSamHost()) {
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

        private fun isSamHost(): Boolean {
            return ilHost == IlHost.SAM_PROD.toString() ||
                ilHost == IlHost.SAM_STAGE.toString() ||
                ilHost == IlHost.SAM_TEST.toString()
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
