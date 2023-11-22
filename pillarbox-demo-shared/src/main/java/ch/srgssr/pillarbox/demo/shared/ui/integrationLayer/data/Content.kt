/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data

import java.text.DateFormat
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Content to abstract content type.
 */
sealed interface Content {
    /**
     * Media.
     *
     * @param media The data received from the Integration Layer.
     */
    data class Media(private val media: ch.srg.dataProvider.integrationlayer.data.remote.Media) : Content {
        /**
         * @property date The date of the media.
         */
        val date: String = DateFormat.getDateInstance().format(media.date)

        /**
         * @property description The description of the media.
         */
        val description = media.description

        /**
         * @property duration The formatted duration of the media.
         */
        val duration = media.duration.toDuration(DurationUnit.MILLISECONDS).toString(unit = DurationUnit.MINUTES)

        /**
         * @property imageTitle The image image of the media.
         */
        val imageTitle = media.imageTitle

        /**
         * @property imageUrl The image URL of the media.
         */
        val imageUrl = media.imageUrl.rawUrl

        /**
         * @property mediaType The type of media.
         */
        val mediaType = media.mediaType

        /**
         * @property showTitle The title of the optional related show.
         */
        val showTitle = media.show?.title

        /**
         * @property title The title of the media.
         */
        val title = media.title

        /**
         * @property urn The URN of the media.
         */
        val urn = media.urn
    }

    /**
     * Show.
     *
     * @param show The data received from the Integration Layer.
     */
    data class Show(private val show: ch.srg.dataProvider.integrationlayer.data.remote.Show) : Content {
        /**
         * @property imageTitle The image image of the show.
         */
        val imageTitle = show.imageTitle

        /**
         * @property imageUrl The image URL of the show.
         */
        val imageUrl = show.imageUrl.rawUrl

        /**
         * @property title The title of the show.
         */
        val title = show.title

        /**
         * @property urn The URN of the show.
         */
        val urn = show.urn
    }

    /**
     * Topic.
     *
     * @param topic The data received from the Integration Layer.
     */
    data class Topic(private val topic: ch.srg.dataProvider.integrationlayer.data.remote.Topic) : Content {
        /**
         * @property imageTitle The image image of the topic.
         */
        val imageTitle = topic.imageTitle

        /**
         * @property imageUrl The image URL of the topic.
         */
        val imageUrl = topic.imageUrl?.rawUrl

        /**
         * @property title The title of the topic.
         */
        val title = topic.title

        /**
         * @property urn The URN of the topic.
         */
        val urn = topic.urn
    }
}
