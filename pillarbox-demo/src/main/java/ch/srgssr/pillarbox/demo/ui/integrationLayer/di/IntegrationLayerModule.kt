/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer.di

import android.app.Application
import ch.srg.dataProvider.integrationlayer.dependencies.components.DataProviderDependencies
import ch.srg.dataProvider.integrationlayer.dependencies.components.IlDataProviderComponent
import ch.srgssr.dataprovider.paging.dependencies.DataProviderPagingComponent
import ch.srgssr.dataprovider.paging.dependencies.DataProviderPagingDependencies
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
    fun createIlRepository(application: Application, ilHost: URL = IlHost.PROD): ILRepository {
        val dataProviderComponent = createDataProviderComponent(application, ilHost)
        val dataProviderPaging = createPagingDataProviderComponent(dataProviderComponent).dataProviderPaging
        return ILRepository(dataProviderPaging = dataProviderPaging, ilService = dataProviderComponent.ilService)
    }

    private fun createDataProviderComponent(application: Application, ilHost: URL = IlHost.PROD): IlDataProviderComponent {
        return DataProviderDependencies.create(application, providerIlHostFromUrl(ilHost))
    }

    private fun createPagingDataProviderComponent(dataProviderComponent: IlDataProviderComponent): DataProviderPagingComponent {
        return DataProviderPagingDependencies.create(dataProviderComponent)
    }

    private fun providerIlHostFromUrl(ilHost: URL): ch.srg.dataProvider.integrationlayer.request.IlHost {
        return when (ilHost) {
            IlHost.STAGE -> ch.srg.dataProvider.integrationlayer.request.IlHost.STAGE
            IlHost.TEST -> ch.srg.dataProvider.integrationlayer.request.IlHost.TEST
            else -> ch.srg.dataProvider.integrationlayer.request.IlHost.PROD
        }
    }
}
