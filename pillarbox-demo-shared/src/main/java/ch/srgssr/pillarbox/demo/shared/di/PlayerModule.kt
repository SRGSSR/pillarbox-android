/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.di

import android.content.Context
import ch.srg.dataProvider.integrationlayer.dependencies.modules.IlServiceModule
import ch.srg.dataProvider.integrationlayer.dependencies.modules.OkHttpModule
import ch.srgssr.dataprovider.paging.DataProviderPaging
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.source.BlockedTimeRangeAssetLoader
import ch.srgssr.pillarbox.demo.shared.source.CustomAssetLoader
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import java.net.URL

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
        )
    }

    /**
     * Create il repository
     */
    fun createIlRepository(context: Context, ilHost: URL = IlHost.DEFAULT): ILRepository {
        val okHttp = OkHttpModule.createOkHttpClient(context)
        val ilService = IlServiceModule.createIlService(okHttp, ilHost = providerIlHostFromUrl(ilHost))
        return ILRepository(dataProviderPaging = DataProviderPaging(ilService), ilService = ilService)
    }

    private fun providerIlHostFromUrl(ilHost: URL): ch.srg.dataProvider.integrationlayer.request.IlHost {
        return when (ilHost) {
            IlHost.STAGE -> ch.srg.dataProvider.integrationlayer.request.IlHost.STAGE
            IlHost.TEST -> ch.srg.dataProvider.integrationlayer.request.IlHost.TEST
            else -> ch.srg.dataProvider.integrationlayer.request.IlHost.PROD
        }
    }
}
