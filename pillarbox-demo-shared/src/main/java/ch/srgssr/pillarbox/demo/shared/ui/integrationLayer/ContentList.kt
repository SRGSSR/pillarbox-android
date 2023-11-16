/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer

import androidx.navigation.NavBackStackEntry
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.RadioChannel

private const val RootRoute = "content"

/**
 * Content list that handle destination route
 */
@Suppress("UndocumentedPublicFunction", "UndocumentedPublicProperty", "UndocumentedPublicClass")
sealed interface ContentList {
    fun getDestinationRoute(): String

    fun getDestinationTitle(): String

    interface ContentListWithBu : ContentList {
        val bu: Bu

        override fun getDestinationTitle(): String {
            return bu.name.uppercase()
        }
    }

    interface ContentListWithRadioChannel : ContentList {
        val radioChannel: RadioChannel

        override fun getDestinationTitle(): String {
            return radioChannel.label
        }
    }

    interface ContentListFactory<T : ContentList> {
        val route: String
        val trackerTitle: String

        fun parse(backStackEntry: NavBackStackEntry): T
    }

    data class TVTopics(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/topics"
        }

        companion object : ContentListFactory<TVTopics> {
            override val route = "$RootRoute/{bu}/tv/topics"
            override val trackerTitle = "tv-topics"

            override fun parse(backStackEntry: NavBackStackEntry): TVTopics {
                return TVTopics(backStackEntry.readBu())
            }
        }
    }

    data class LatestMediaForTopic(
        val urn: String,
        val topic: String
    ) : ContentList {
        override fun getDestinationRoute(): String {
            return "$RootRoute/latestMediaByTopic/$urn?topic=$topic"
        }

        override fun getDestinationTitle(): String {
            return topic
        }

        companion object : ContentListFactory<LatestMediaForTopic> {
            override val route = "$RootRoute/latestMediaByTopic/{topicUrn}?topic={topic}"
            override val trackerTitle = "latest-media-for-topic"

            override fun parse(backStackEntry: NavBackStackEntry): LatestMediaForTopic {
                val arguments = backStackEntry.arguments

                return LatestMediaForTopic(
                    urn = arguments?.getString("topicUrn").orEmpty(),
                    topic = arguments?.getString("topic").orEmpty()
                )
            }
        }
    }

    data class LatestMediaForShow(
        val urn: String,
        val show: String,
    ) : ContentList {
        override fun getDestinationRoute(): String {
            return "$RootRoute/latestMediaByShow/$urn?show=$show"
        }

        override fun getDestinationTitle(): String {
            return show
        }

        companion object : ContentListFactory<LatestMediaForShow> {
            override val route = "$RootRoute/latestMediaByShow/{showUrn}?show={show}"
            override val trackerTitle = "latest-media-for-show"

            override fun parse(backStackEntry: NavBackStackEntry): LatestMediaForShow {
                val arguments = backStackEntry.arguments

                return LatestMediaForShow(
                    urn = arguments?.getString("showUrn").orEmpty(),
                    show = arguments?.getString("show").orEmpty(),
                )
            }
        }
    }

    data class TVShows(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/shows"
        }

        companion object : ContentListFactory<TVShows> {
            override val route = "$RootRoute/{bu}/tv/shows"
            override val trackerTitle = "tv-shows"

            override fun parse(backStackEntry: NavBackStackEntry): TVShows {
                return TVShows(backStackEntry.readBu())
            }
        }
    }

    data class TVLatestMedias(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/latestMedia"
        }

        companion object : ContentListFactory<TVLatestMedias> {
            override val route = "$RootRoute/{bu}/tv/latestMedia"
            override val trackerTitle = "tv-latest-videos"

            override fun parse(backStackEntry: NavBackStackEntry): TVLatestMedias {
                return TVLatestMedias(backStackEntry.readBu())
            }
        }
    }

    data class TVLivestreams(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/livestream"
        }

        companion object : ContentListFactory<TVLivestreams> {
            override val route = "$RootRoute/{bu}/tv/livestream"
            override val trackerTitle = "tv-livestreams"

            override fun parse(backStackEntry: NavBackStackEntry): TVLivestreams {
                return TVLivestreams(backStackEntry.readBu())
            }
        }
    }

    data class TVLiveCenter(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/livecenter"
        }

        companion object : ContentListFactory<TVLiveCenter> {
            override val route = "$RootRoute/{bu}/tv/livecenter"
            override val trackerTitle = "live-center"

            override fun parse(backStackEntry: NavBackStackEntry): TVLiveCenter {
                return TVLiveCenter(backStackEntry.readBu())
            }
        }
    }

    data class TVLiveWeb(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/liveweb"
        }

        companion object : ContentListFactory<TVLiveWeb> {
            override val route = "$RootRoute/{bu}/tv/liveweb"
            override val trackerTitle = "live-web"

            override fun parse(backStackEntry: NavBackStackEntry): TVLiveWeb {
                return TVLiveWeb(backStackEntry.readBu())
            }
        }
    }

    data class RadioLiveStreams(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/radio/livestream"
        }

        companion object : ContentListFactory<RadioLiveStreams> {
            override val route = "$RootRoute/{bu}/radio/livestream"
            override val trackerTitle = "radio-livestreams"

            override fun parse(backStackEntry: NavBackStackEntry): RadioLiveStreams {
                return RadioLiveStreams(backStackEntry.readBu())
            }
        }
    }

    data class RadioShows(override val radioChannel: RadioChannel) : ContentListWithRadioChannel {
        override fun getDestinationRoute(): String {
            return "$RootRoute/${radioChannel.bu}/radio/shows/$radioChannel"
        }

        companion object : ContentListFactory<RadioShows> {
            override val route = "$RootRoute/{bu}/radio/shows/{radioChannel}"
            override val trackerTitle = "shows"

            override fun parse(backStackEntry: NavBackStackEntry): RadioShows {
                return RadioShows(backStackEntry.readRadioChannel())
            }
        }
    }

    data class RadioLatestMedias(override val radioChannel: RadioChannel) : ContentListWithRadioChannel {
        override fun getDestinationRoute(): String {
            return "$RootRoute/${radioChannel.bu}/radio/latestMedia/$radioChannel"
        }

        companion object : ContentListFactory<RadioLatestMedias> {
            override val route = "$RootRoute/{bu}/radio/latestMedia/{radioChannel}"
            override val trackerTitle = "latest-audios"

            override fun parse(backStackEntry: NavBackStackEntry): RadioLatestMedias {
                return RadioLatestMedias(backStackEntry.readRadioChannel())
            }
        }
    }
}

private fun NavBackStackEntry.readBu(): Bu {
    return arguments?.getString("bu")?.let { Bu(it) } ?: Bu.RTS
}

private fun NavBackStackEntry.readRadioChannel(): RadioChannel {
    return arguments?.getString("radioChannel")?.let { RadioChannel.valueOf(it) } ?: RadioChannel.RTR
}
