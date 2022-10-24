/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.player.data.MediaItemSource
import java.util.regex.Pattern

/**
 * Load MediaItem from [urnMediaItemSource] if the media uri is an urn.
 *
 * In the demo application we are mixing url and urn. To simplify the data, we choose to store
 * urn and url in the media item uri when building the MediaItem with [MediaItem.Builder.setUri].
 *
 * So we need to parse the uri and choose if we have to load it with [urnMediaItemSource] or using the mediaItem himself.
 *
 * @property urnMediaItemSource item source to use with urn
 */
class MixedMediaItemSource(
    private val urnMediaItemSource: MediaCompositionMediaItemSource
) : MediaItemSource {
    private val pattern = Pattern.compile(MEDIA_URN)

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        val uri = mediaItem.localConfiguration?.uri.toString()
        return if (pattern.matcher(uri).matches()) {
            // Add mediaId to avoid exception
            urnMediaItemSource.loadMediaItem(mediaItem.buildUpon().setMediaId(uri).build())
        } else {
            mediaItem
        }
    }

    companion object {
        /**
         * Pattern used by Integration layer to identify a media urn
         */
        private const val MEDIA_URN = "urn:(srf|rtr|rts|rsi|swi|swisstxt)(:(livestream|scheduled_livestream))?(:ssatr)?:(video|video:clip|audio)(:" +
            "(srf|rtr|rts|rsi|swi))?:((\\d+|[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}|[0-9a-fA-F]{8}-" +
            "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}_program_\\d+|livestream_\\S*|[0-9a-z\\-]+(_\\w+)?)" +
            "|(rsc-de|rsc-fr|rsc-it|rsj|rsp))"
    }
}
