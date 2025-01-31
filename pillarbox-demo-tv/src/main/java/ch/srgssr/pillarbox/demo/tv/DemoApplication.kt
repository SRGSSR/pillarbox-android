/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.SenderDisconnectedEventInfo
import com.google.android.gms.cast.tv.SenderInfo

/**
 * Demo application that sets up Coil.
 */
class DemoApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        CastReceiverContext.initInstance(this)
        CastReceiverContext.getInstance().registerEventCallback(object : CastReceiverContext.EventCallback() {
            override fun onSenderConnected(p0: SenderInfo) {
                Toast.makeText(this@DemoApplication, "Sender connected ${p0.senderId}", Toast.LENGTH_SHORT).show()
            }

            override fun onSenderDisconnected(p0: SenderDisconnectedEventInfo) {
                Toast.makeText(this@DemoApplication, "Sender disconnected", Toast.LENGTH_SHORT).show()
            }
        })
        ProcessLifecycleOwner.get().lifecycle.addObserver(MyLifecycleObserver())
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

    // Create a LifecycleObserver class.
    private inner class MyLifecycleObserver : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            // App prepares to enter foreground.
            CastReceiverContext.getInstance().start()
        }

        override fun onStop(owner: LifecycleOwner) {
            // App has moved to the background or has terminated.
            CastReceiverContext.getInstance().stop()
        }
    }
}
