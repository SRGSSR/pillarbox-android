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
 * Creates a [PillarboxExoPlayer] instance configured for the SRG SSR.
 *
 * **Basic usage**
 *
 * ```kotlin
 * val srgPlayer = PillarboxExoPlayer(context)
 * ```
 *
 * This creates a player with the default SRG SSR configuration:
 * - Automatic integration with Pillarbox Monitoring: playback events are sent to a predefined endpoint.
 * - SRG Asset Loader: integrates an [SRGAssetLoader] for handling SRG-specific media resources. If not explicitly configured, a default
 * instance is created.
 *
 * **Custom configuration**
 *
 * ```kotlin
 * val customSrgPlayer = PillarboxExoPlayer(context) {
 *     srgAssetLoader(context) {
 *          mediaCompositionService(CustomMediaCompositionService())
 *     }
 * }
 * ```
 *
 * @param context The [Context] of the application.
 * @param builder An optional lambda with a receiver of type [SRG.Builder] allowing customization of the player's configuration.
 *
 * @return A configured [PillarboxExoPlayer] instance ready for playback.
 */
@PillarboxDsl
fun PillarboxExoPlayer(
    context: Context,
    builder: SRG.Builder.() -> Unit = {},
): PillarboxExoPlayer {
    return PillarboxExoPlayer(context, SRG, builder)
}

/**
 * Pillarbox player configuration for the SRG. It sets up all SRG components by default.
 *
 * To create a Pillarbox player with this configuration, use [PillarboxExoPlayer][ch.srgssr.pillarbox.core.business.PillarboxExoPlayer].
 */
@Suppress("MatchingDeclarationName")
object SRG : PlayerConfig<SRG.Builder> {

    override fun create(): Builder {
        return Builder()
    }

    /**
     * Builder for creating an SRG-flavored Pillarbox player.
     */
    class Builder internal constructor() : PillarboxBuilder() {
        init {
            val url = if (BuildConfig.DEBUG) "https://dev.monitoring.pillarbox.ch/api/events" else "https://monitoring.pillarbox.ch/api/events"
            monitoring(url)
            seekForwardIncrement(30.seconds)
            seekBackwardIncrement(10.seconds)
        }

        private var srgAssetLoader: SRGAssetLoader? = null

        /**
         * Configures and adds an [SRGAssetLoader] to the player.
         *
         * **Note:** this function should be called only once. Subsequent calls will result in an exception.
         *
         * @param context The [Context] required for the [SRGAssetLoader].
         * @param block A lambda to configure the [SRGAssetLoader] using a [SRGAssetLoaderConfig] instance.
         *
         * @throws IllegalStateException If an [SRGAssetLoader] has already been configured.
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
