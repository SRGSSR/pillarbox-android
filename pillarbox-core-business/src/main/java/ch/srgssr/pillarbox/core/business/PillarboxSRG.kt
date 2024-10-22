/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoaderConfig
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.dsl.PillarboxDsl
import ch.srgssr.pillarbox.player.dsl.PlayerBuilder
import ch.srgssr.pillarbox.player.dsl.PlayerConfig
import ch.srgssr.pillarbox.player.dsl.Remote
import ch.srgssr.pillarbox.player.dsl.Remote.config
import ch.srgssr.pillarbox.player.dsl.pillarbox
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.seconds

/**
 * Pillarbox exoplayer configured for the SRG SSR.
 *
 * @param context The [Context].
 * @param config The config.
 * @receiver [SRG.Builder].
 * @return The configured for SRG [PillarboxExoPlayer].
 */
@Suppress("FunctionName")
@PillarboxDsl
fun PillarboxExoplayer(context: Context, config: SRG.Builder.() -> Unit): PillarboxExoPlayer {
    return pillarbox(context, SRG, config)
}

/**
 * Pillarbox player configuration for SRG.
 * It's setup all SRG components by default.
 */
@Suppress("MatchingDeclarationName")
object SRG : PlayerConfig<SRG.Builder> {

    override fun create(): Builder {
        return Builder()
    }

    /**
     * Builder for the SRG.
     */
    class Builder : PlayerBuilder() {
        init {
            val url = if (BuildConfig.DEBUG) "https://dev.monitoring.pillarbox.ch/api/events" else "https://monitoring.pillarbox.ch/api/events"
            monitoring(url)
            seekForwardIncrement(30.seconds)
            seekBackwardIncrement(10.seconds)
        }

        private var srgAssetLoader: SRGAssetLoader? = null

        /**
         * Monitoring
         *
         * @param endpointUrl The monitoring endpoint url.
         * @param httpClient The [HttpClient].
         * @param coroutineScope The [CoroutineScope].
         */
        fun monitoring(
            endpointUrl: String,
            httpClient: HttpClient? = null,
            coroutineScope: CoroutineScope? = null,
        ) {
            monitoring(Remote) {
                config(endpointUrl = endpointUrl, httpClient = httpClient, coroutineScope = coroutineScope)
            }
        }

        /**
         * Configure [SRGAssetLoader].
         *
         * @param context The context.
         * @param block The block that configures [SRGAssetLoader].
         * @receiver [SRGAssetLoaderConfig].
         */
        fun srgAssetLoader(context: Context, block: SRGAssetLoaderConfig.() -> Unit) {
            check(srgAssetLoader == null)
            srgAssetLoader = SRGAssetLoader(context, block)
            srgAssetLoader?.let {
                addAssetLoader(it)
            }
        }
    }
}
