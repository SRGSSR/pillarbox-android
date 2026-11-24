/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.net.Uri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.UpdatableMediaItem

/**
 * An update of the content being played, sent from the media source to the player.
 *
 * @property pillarboxMetadata The new [PillarboxMetadata].
 * @property mediaMetadata The new [MediaMetadata].
 * @property reloadRequired Whether the stream URL changed, so the media item has to be reloaded to play the new stream.
 */
internal data class PillarboxUpdate(
    val pillarboxMetadata: PillarboxMetadata,
    val mediaMetadata: MediaMetadata,
    val reloadRequired: Boolean,
) {
    companion object {
        /**
         * Creates a [PillarboxUpdate] from [update], comparing its stream URL with the one being played.
         *
         * @param update The [UpdatableMediaItem] emitted by the asset loader.
         * @param playingMediaItemUri The URI of the stream being played.
         */
        fun from(update: UpdatableMediaItem, playingMediaItemUri: Uri?): PillarboxUpdate {
            return PillarboxUpdate(
                pillarboxMetadata = update.pillarboxMetadata,
                mediaMetadata = update.mediaMetadata,
                reloadRequired = update.mediaItem.localConfiguration?.uri != playingMediaItemUri,
            )
        }
    }
}
