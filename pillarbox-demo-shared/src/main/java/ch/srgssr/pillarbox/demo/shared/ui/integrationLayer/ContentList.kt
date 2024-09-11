/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer

import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import kotlinx.serialization.Serializable

/**
 * Content list that handle destination route
 */
@Serializable
@Suppress("UndocumentedPublicProperty", "UndocumentedPublicClass")
sealed interface ContentList {
    val destinationTitle: String

    // Type-safe navigation does not yet support property-level @Serializable
    // So, we use the BU name as a property, and recreate the BU on demand
    // See: https://issuetracker.google.com/issues/348468840
    sealed interface ContentListWithBu : ContentList {
        val buName: String

        val bu: Bu
            get() = Bu(buName)

        override val destinationTitle: String
            get() = bu.name.uppercase()
    }

    @Serializable
    @ConsistentCopyVisibility
    data class TVTopics private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    data class LatestMediaForTopic(
        val urn: String,
        val topic: String
    ) : ContentList {
        override val destinationTitle = topic
    }

    @Serializable
    data class LatestMediaForShow(
        val urn: String,
        val show: String
    ) : ContentList {
        override val destinationTitle = show
    }

    @Serializable
    @ConsistentCopyVisibility
    data class TVShows private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class TVLatestMedias private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class TVLivestreams private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class TVLiveCenter private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class TVLiveWeb private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class RadioLiveStreams private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class RadioShows private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class RadioShowsForChannel private constructor(
        override val buName: String,
        val channelId: String,
        private val channelTitle: String,
    ) : ContentListWithBu {
        override val destinationTitle = channelTitle

        constructor(bu: Bu, channelId: String, channelTitle: String) : this(bu.name, channelId, channelTitle)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class RadioLatestMedias private constructor(
        override val buName: String,
    ) : ContentListWithBu {
        constructor(bu: Bu) : this(bu.name)
    }

    @Serializable
    @ConsistentCopyVisibility
    data class RadioLatestMediasForChannel private constructor(
        override val buName: String,
        val channelId: String,
        private val channelTitle: String,
    ) : ContentListWithBu {
        override val destinationTitle = channelTitle

        constructor(bu: Bu, channelId: String, channelTitle: String) : this(bu.name, channelId, channelTitle)
    }
}
