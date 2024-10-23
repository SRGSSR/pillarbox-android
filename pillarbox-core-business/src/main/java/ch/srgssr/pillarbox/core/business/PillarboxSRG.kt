/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoaderConfig
import ch.srgssr.pillarbox.player.PillarboxBuilder
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PlayerConfig
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
            return super.createExoPlayerBuilder(context)
        }
    }
}
