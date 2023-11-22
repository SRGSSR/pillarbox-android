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
        val duration = media.duration.toDuration(DurationUnit.MILLISECONDS).toString()
        val imageTitle = media.imageTitle
        val imageUrl = media.imageUrl.rawUrl
        val mediaType = media.mediaType
        val showTitle = media.show?.title
        val title = media.title
        val urn = media.urn
    }

    /**
     * Show
     *
     * @property show from Integration layer
     */
    data class Show(val show: ch.srg.dataProvider.integrationlayer.data.remote.Show) : Content

    /**
     * Topic
     *
     * @property topic from Integration layer
     */
    data class Topic(val topic: ch.srg.dataProvider.integrationlayer.data.remote.Topic) : Content
}
