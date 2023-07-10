/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer.data

/**
 * Content to abstract content type.
 */
sealed interface Content {

    /**
     * Media
     *
     * @property media from Integration layer
     */
    data class Media(val media: ch.srg.dataProvider.integrationlayer.data.remote.Media) : Content

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
