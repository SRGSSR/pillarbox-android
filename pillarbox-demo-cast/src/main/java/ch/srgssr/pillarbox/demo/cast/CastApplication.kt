/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp.invoke
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.google.android.gms.cast.framework.CastContext

/**
 * [CastApplication] initializes Google Cast functionality by setting up the [CastContext].
 */
class CastApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        getCastContext()
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { PillarboxOkHttp() },
                        cacheStrategy = { CacheControlCacheStrategy() },
                    )
                )
            }
            .build()
    }
}
