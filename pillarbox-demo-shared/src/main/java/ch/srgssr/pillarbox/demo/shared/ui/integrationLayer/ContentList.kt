/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer

import androidx.navigation.NavBackStackEntry
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu

private const val RootRoute = "content"

/**
 * Content list that handle destination route
 */
@Suppress("UndocumentedPublicFunction", "UndocumentedPublicProperty", "UndocumentedPublicClass")
sealed interface ContentList {
    val destinationRoute: String

    val destinationTitle: String

    sealed interface ContentListWithBu : ContentList {
        val bu: Bu

        override val destinationTitle: String
            get() = bu.name.uppercase()
    }

    interface ContentListFactory<T : ContentList> {
        val route: String
        val trackerTitle: String

        fun parse(backStackEntry: NavBackStackEntry): T
    }

    data class TVTopics(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/tv/topics"

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
        override val destinationRoute = "$RootRoute/latestMediaByTopic/$urn?topic=$topic"

        override val destinationTitle = topic

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
        override val destinationRoute = "$RootRoute/latestMediaByShow/$urn?show=$show"

        override val destinationTitle = show

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
        override val destinationRoute = "$RootRoute/$bu/tv/shows"

        companion object : ContentListFactory<TVShows> {
            override val route = "$RootRoute/{bu}/tv/shows"
            override val trackerTitle = "tv-shows"

            override fun parse(backStackEntry: NavBackStackEntry): TVShows {
                return TVShows(backStackEntry.readBu())
            }
        }
    }

    data class TVLatestMedias(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/tv/latestMedia"

        companion object : ContentListFactory<TVLatestMedias> {
            override val route = "$RootRoute/{bu}/tv/latestMedia"
            override val trackerTitle = "tv-latest-videos"

            override fun parse(backStackEntry: NavBackStackEntry): TVLatestMedias {
                return TVLatestMedias(backStackEntry.readBu())
            }
        }
    }

    data class TVLivestreams(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/tv/livestream"

        companion object : ContentListFactory<TVLivestreams> {
            override val route = "$RootRoute/{bu}/tv/livestream"
            override val trackerTitle = "tv-livestreams"

            override fun parse(backStackEntry: NavBackStackEntry): TVLivestreams {
                return TVLivestreams(backStackEntry.readBu())
            }
        }
    }

    data class TVLiveCenter(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/tv/livecenter"

        companion object : ContentListFactory<TVLiveCenter> {
            override val route = "$RootRoute/{bu}/tv/livecenter"
            override val trackerTitle = "live-center"

            override fun parse(backStackEntry: NavBackStackEntry): TVLiveCenter {
                return TVLiveCenter(backStackEntry.readBu())
            }
        }
    }

    data class TVLiveWeb(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/tv/liveweb"

        companion object : ContentListFactory<TVLiveWeb> {
            override val route = "$RootRoute/{bu}/tv/liveweb"
            override val trackerTitle = "live-web"

            override fun parse(backStackEntry: NavBackStackEntry): TVLiveWeb {
                return TVLiveWeb(backStackEntry.readBu())
            }
        }
    }

    data class RadioLiveStreams(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/radio/livestream"

        companion object : ContentListFactory<RadioLiveStreams> {
            override val route = "$RootRoute/{bu}/radio/livestream"
            override val trackerTitle = "radio-livestreams"

            override fun parse(backStackEntry: NavBackStackEntry): RadioLiveStreams {
                return RadioLiveStreams(backStackEntry.readBu())
            }
        }
    }

    data class RadioShows(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/radio/shows"

        companion object : ContentListFactory<RadioShows> {
            override val route = "$RootRoute/{bu}/radio/shows"
            override val trackerTitle = "shows"

            override fun parse(backStackEntry: NavBackStackEntry): RadioShows {
                return RadioShows(backStackEntry.readBu())
            }
        }
    }

    data class RadioShowsForChannel(
        override val bu: Bu,
        val channelId: String,
        private val channelTitle: String
    ) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/radio/shows/$channelId/$channelTitle"
        override val destinationTitle = channelTitle

        companion object : ContentListFactory<RadioShowsForChannel> {
            override val route = "$RootRoute/{bu}/radio/shows/{channelId}/{channelTitle}"
            override val trackerTitle = "shows-for-channel"

            override fun parse(backStackEntry: NavBackStackEntry): RadioShowsForChannel {
                return RadioShowsForChannel(
                    bu = backStackEntry.readBu(),
                    channelId = backStackEntry.readChannelId(),
                    channelTitle = backStackEntry.readChannelTitle()
                )
            }
        }
    }

    data class RadioLatestMedias(override val bu: Bu) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/radio/latestMedia"

        companion object : ContentListFactory<RadioLatestMedias> {
            override val route = "$RootRoute/{bu}/radio/latestMedia"
            override val trackerTitle = "latest-audios"

            override fun parse(backStackEntry: NavBackStackEntry): RadioLatestMedias {
                return RadioLatestMedias(backStackEntry.readBu())
            }
        }
    }

    data class RadioLatestMediasForChannel(
        override val bu: Bu,
        val channelId: String,
        private val channelTitle: String
    ) : ContentListWithBu {
        override val destinationRoute = "$RootRoute/$bu/radio/latestMedia/$channelId/$channelTitle"
        override val destinationTitle = channelTitle

        companion object : ContentListFactory<RadioLatestMediasForChannel> {
            override val route = "$RootRoute/{bu}/radio/latestMedia/{channelId}/{channelTitle}"
            override val trackerTitle = "latest-audios-for-channel"

            override fun parse(backStackEntry: NavBackStackEntry): RadioLatestMediasForChannel {
                return RadioLatestMediasForChannel(
                    bu = backStackEntry.readBu(),
                    channelId = backStackEntry.readChannelId(),
                    channelTitle = backStackEntry.readChannelTitle()
                )
            }
        }
    }
}

private fun NavBackStackEntry.readBu(): Bu {
    return arguments?.getString("bu")?.let { Bu(it) } ?: Bu.RTS
}

private fun NavBackStackEntry.readChannelId(): String {
    return arguments?.getString("channelId").orEmpty()
}

private fun NavBackStackEntry.readChannelTitle(): String {
    return arguments?.getString("channelTitle").orEmpty()
}
