/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.di

import android.content.Context
import ch.srg.dataProvider.integrationlayer.dependencies.modules.IlServiceModule
import ch.srg.dataProvider.integrationlayer.dependencies.modules.OkHttpModule
import ch.srgssr.dataprovider.paging.DataProviderPaging
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.images.DefaultImageScalingService
import ch.srgssr.pillarbox.core.business.images.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector.getVector
import ch.srgssr.pillarbox.demo.shared.data.MixedMediaItemSource
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.player.PillarboxPlayer
import java.net.URL

/**
 * Dependencies to make custom Dependency Injection
 */
object PlayerModule {

    private fun provideIntegrationLayerItemSource(context: Context): MediaCompositionMediaItemSource =
        MediaCompositionMediaItemSource(
            mediaCompositionDataSource = DefaultMediaCompositionDataSource(vector = context.getVector()),
            imageScalingService = provideImageScalingService()
        )

    /**
     * Provide mixed item source that load Url and Urn
     */
    fun provideMixedItemSource(context: Context): MixedMediaItemSource = MixedMediaItemSource(
        provideIntegrationLayerItemSource(context)
    )

    /**
     * Provide default player that allow to play urls and urns content from the SRG
     */
    fun provideDefaultPlayer(context: Context): PillarboxPlayer {
        return DefaultPillarbox(context = context, mediaItemSource = provideMixedItemSource(context))
    }

    /**
     * Create il repository
     */
    fun createIlRepository(context: Context, ilHost: URL = IlHost.DEFAULT): ILRepository {
        val okHttp = OkHttpModule.createOkHttpClient(context)
        val ilService = IlServiceModule.createIlService(okHttp, ilHost = providerIlHostFromUrl(ilHost))
        return ILRepository(dataProviderPaging = DataProviderPaging(ilService), ilService = ilService)
    }

    /**
     * Provide a default implementation for the image scaling service.
     */
    fun provideImageScalingService(): ImageScalingService {
        return DefaultImageScalingService()
    }

    private fun providerIlHostFromUrl(ilHost: URL): ch.srg.dataProvider.integrationlayer.request.IlHost {
        return when (ilHost) {
            IlHost.STAGE -> ch.srg.dataProvider.integrationlayer.request.IlHost.STAGE
            IlHost.TEST -> ch.srg.dataProvider.integrationlayer.request.IlHost.TEST
            else -> ch.srg.dataProvider.integrationlayer.request.IlHost.PROD
        }
    }
}
