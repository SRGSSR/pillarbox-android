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
     * @property media The data received from the Integration Layer.
     * @property date The date of the media.
     * @property description The description of the media.
     * @property duration The formatted duration of the media.
     * @property imageTitle The image image of the media.
     * @property imageUrl The image URL of the media.
     * @property mediaType The type of media.
     * @property showTitle The title of the optional related show.
     * @property title The title of the media.
     * @property urn The URN of the media.
     */
    @Suppress("OutdatedDocumentation", "UndocumentedPublicProperty") // Fixed in Detekt 1.23.0 (https://github.com/detekt/detekt/pull/6061)
    data class Media(private val media: ch.srg.dataProvider.integrationlayer.data.remote.Media) : Content {
        val date: String = DateFormat.getDateInstance().format(media.date)
        val description = media.description
        val duration = media.duration.toDuration(DurationUnit.MILLISECONDS).toString(unit = DurationUnit.MINUTES)
        val imageTitle = media.imageTitle
        val imageUrl = media.imageUrl.rawUrl
        val mediaType = media.mediaType
        val showTitle = media.show?.title
        val title = media.title
        val urn = media.urn
    }

    /**
     * Show.
     *
     * @property show The data received from the Integration Layer.
     * @property imageTitle The image image of the show.
     * @property imageUrl The image URL of the show.
     * @property title The title of the show.
     * @property urn The URN of the show.
     */
    @Suppress("OutdatedDocumentation", "UndocumentedPublicProperty") // Fixed in Detekt 1.23.0 (https://github.com/detekt/detekt/pull/6061)
    data class Show(private val show: ch.srg.dataProvider.integrationlayer.data.remote.Show) : Content {
        val imageTitle = show.imageTitle
        val imageUrl = show.imageUrl.rawUrl
        val title = show.title
        val urn = show.urn
    }

    /**
     * Topic.
     *
     * @property topic The data received from the Integration Layer.
     * @property imageTitle The image image of the topic.
     * @property imageUrl The image URL of the topic.
     * @property title The title of the topic.
     * @property urn The URN of the topic.
     */
    @Suppress("OutdatedDocumentation", "UndocumentedPublicProperty") // Fixed in Detekt 1.23.0 (https://github.com/detekt/detekt/pull/6061)
    data class Topic(private val topic: ch.srg.dataProvider.integrationlayer.data.remote.Topic) : Content {
        val imageTitle = topic.imageTitle
        val imageUrl = topic.imageUrl?.rawUrl
        val title = topic.title
        val urn = topic.urn
    }
}
