/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.image.ExternallyLoadedImageDecoder
import androidx.media3.exoplayer.image.ExternallyLoadedImageDecoder.BitmapResolver
import androidx.media3.exoplayer.image.ImageDecoder
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoaderConfig
import ch.srgssr.pillarbox.core.business.source.SRGImageRenderer
import ch.srgssr.pillarbox.player.PillarboxBuilder
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PlayerConfig
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

/**
 * Pillarbox ExoPlayer configured for the SRG SSR.
 *
 * @param context The [Context].
 * @param builder The builder.
 * @receiver [SRG.Builder].
 * @return The configured [PillarboxExoplayer] for SRG SSR.
 */
@Suppress("FunctionName")
@PillarboxDsl
fun PillarboxExoplayer(
    context: Context,
    builder: SRG.Builder.() -> Unit = {},
): PillarboxExoPlayer {
    return SRG.create()
        .apply(builder)
        .create(context)
}

/**
 * Pillarbox player configuration for the SRG.
 * It sets up all SRG components by default.
 */
@Suppress("MatchingDeclarationName")
object SRG : PlayerConfig<SRG.Builder> {

    override fun create(): Builder {
        return Builder()
    }

    /**
     * Builder for the SRG.
     */
    class Builder : PillarboxBuilder() {
        init {
            val url = if (BuildConfig.DEBUG) "https://dev.monitoring.pillarbox.ch/api/events" else "https://monitoring.pillarbox.ch/api/events"
            monitoring(url)
            seekForwardIncrement(30.seconds)
            seekBackwardIncrement(10.seconds)
        }

        private var srgAssetLoader: SRGAssetLoader? = null

        /**
         * Configure a [SRGAssetLoader].
         *
         * @param context The [Context].
         * @param block The block to configure a [SRGAssetLoader].
         * @receiver [SRGAssetLoaderConfig].
         */
        fun srgAssetLoader(context: Context, block: SRGAssetLoaderConfig.() -> Unit) {
            check(srgAssetLoader == null)
            srgAssetLoader = SRGAssetLoader(context, block)
                .also(::addAssetLoader)
        }

        override fun createExoPlayerBuilder(context: Context): ExoPlayer.Builder {
            if (srgAssetLoader == null) srgAssetLoader(context) {}
            return super.createExoPlayerBuilder(context).apply {
                setRenderersFactory(
                    MyRenderersFactory(context)
                )
            }
        }
    }
}

class MyRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    val bitmapResolver = object : BitmapResolver {
        private var bitmap: Bitmap? = null
        private val lock = Any()
        override fun resolve(request: ExternallyLoadedImageDecoder.ExternalImageRequest): ListenableFuture<Bitmap> {
            return Futures.submit(
                object : Callable<Bitmap> {
                    override fun call(): Bitmap {
                        synchronized(lock) {
                            if (bitmap == null) {
                                bitmap = URL(request.uri.toString()).openStream().use {
                                    BitmapFactory.decodeStream(it)
                                } // use
                            }
                        }
                        return bitmap!!
                    }
                },
                Executors.newSingleThreadExecutor()
            )
        }
    }

    init {
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        setEnableDecoderFallback(true)
    }

    override fun buildImageRenderers(out: ArrayList<Renderer>) {
        out.add(SRGImageRenderer(imageDecoderFactory, null))
    }

    override fun getImageDecoderFactory(): ImageDecoder.Factory {
        return ExternallyLoadedImageDecoder.Factory(bitmapResolver)
    }
}
