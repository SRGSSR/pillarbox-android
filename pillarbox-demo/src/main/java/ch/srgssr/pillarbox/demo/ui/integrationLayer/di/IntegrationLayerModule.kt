/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer.di

import android.app.Application
import ch.srg.dataProvider.integrationlayer.dependencies.modules.IlServiceModule
import ch.srg.dataProvider.integrationlayer.dependencies.modules.OkHttpModule
import ch.srgssr.dataprovider.paging.DataProviderPaging
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.ILRepository
import java.net.URL

/**
 * Integration layer module DI
 */
object IntegrationLayerModule {

    /**
     * Create il repository
     */
    fun createIlRepository(application: Application, ilHost: URL = IlHost.DEFAULT): ILRepository {
        val okHttp = OkHttpModule.createOkHttpClient(application)
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
