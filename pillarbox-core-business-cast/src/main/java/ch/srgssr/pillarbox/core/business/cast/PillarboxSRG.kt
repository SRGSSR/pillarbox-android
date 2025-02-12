/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.cast

import android.content.Context
import ch.srgssr.pillarbox.cast.CastPlayerConfig
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.PillarboxCastPlayerBuilder
import ch.srgssr.pillarbox.player.PillarboxDsl

/**
 * Creates a [PillarboxCastPlayer] instance configured for the SRG SSR.
 *
 * **Basic usage**
 *
 * ```kotlin
 * val srgCastPlayer = PillarboxCastPlayer(context)
 * ```
 *
 * This creates a player with the default SRG SSR configuration:
 * - SRG cast MediaItemConverter: integrates an [SRGMediaItemConverter] for handling SRG-specific media item to cast item. If not explicitly
 * configured, a default instance is created.
 *
 * **Custom configuration**
 *
 * ```kotlin
 * val customSrgCastPlayer = PillarboxCastPlayer(context) {
 *     mediaItemConverter(SRGMediaItemConverter())
 *     onCastSessionAvailable(context) {
 *          setMediaItems(listItems)
 *          play()
 *     }
 *     onCastSessionUnavailable {
 *          goBackToLocalPlayerBack()
 *     }
 * }
 * ```
 *
 * The same can be achieved by using [PillarboxCastPlayer.setSessionAvailabilityListener].
 *
 * @param context The [Context] of the application.
 * @param builder An optional lambda with a receiver of type [SRG.Builder] allowing customization of the player's configuration.
 *
 * @return A configured [PillarboxCastPlayer] instance ready for playback.
 */
@PillarboxDsl
fun PillarboxCastPlayer(
    context: Context,
    builder: SRG.Builder.() -> Unit = {},
): PillarboxCastPlayer {
    return PillarboxCastPlayer(context, SRG, builder)
}

/**
 * Builder for creating an SRG-flavored Pillarbox cast player.
 */
object SRG : CastPlayerConfig<SRG.Builder> {

    override fun create(): Builder {
        return Builder
    }

    /**
     * A builder class for creating and configuring a [PillarboxCastPlayer].
     */
    object Builder : PillarboxCastPlayerBuilder() {
        init {
            mediaItemConverter(SRGMediaItemConverter())
        }
    }
}
