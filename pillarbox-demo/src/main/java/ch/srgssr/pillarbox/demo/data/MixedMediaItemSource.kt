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
 * Load MediaItem from [demoItemDataSource] or [ilItemDataSource]
 *
 * @property demoItemDataSource
 * @property ilItemDataSource
 */
class MixedMediaItemSource(
    private val demoItemDataSource: DemoMediaItemSource,
    private val ilItemDataSource: MediaCompositionMediaItemSource
) : MediaItemSource {

    private val pattern = Pattern.compile(MEDIA_URN)

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        return if (pattern.matcher(mediaItem.mediaId).matches()) {
            ilItemDataSource.loadMediaItem(mediaItem)
        } else {
            demoItemDataSource.loadMediaItem(mediaItem)
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
