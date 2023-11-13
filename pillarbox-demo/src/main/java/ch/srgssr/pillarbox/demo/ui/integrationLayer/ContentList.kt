/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.navigation.NavBackStackEntry
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.RadioChannel

private const val RootRoute = "content"

/**
 * Content list that handle destination route
 */
@Suppress("UndocumentedPublicFunction", "UndocumentedPublicProperty", "UndocumentedPublicClass")
sealed interface ContentList {
    fun getDestinationRoute(): String

    interface ContentListWithBu : ContentList {
        val bu: Bu
    }

    data class TvTopics(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/topics"
        }

        companion object {
            const val route = "$RootRoute/{bu}/tv/topics"

            fun parse(backStackEntry: NavBackStackEntry): TvTopics {
                return TvTopics(backStackEntry.readBu())
            }
        }
    }

    data class LatestMediaForTopic(val urn: String) : ContentList {
        override fun getDestinationRoute(): String {
            return "$RootRoute/latestMediaByTopic/$urn"
        }

        companion object {
            const val route = "$RootRoute/latestMediaByTopic/{topicUrn}"

            fun parse(backStackEntry: NavBackStackEntry): LatestMediaForTopic {
                return LatestMediaForTopic(urn = backStackEntry.arguments?.getString("topicUrn")!!)
            }
        }
    }

    data class LatestMediaForShow(val urn: String) : ContentList {
        override fun getDestinationRoute(): String {
            return "$RootRoute/latestMediaByShow/$urn"
        }

        companion object {
            const val route = "$RootRoute/latestMediaByShow/{showUrn}"

            fun parse(backStackEntry: NavBackStackEntry): LatestMediaForShow {
                return LatestMediaForShow(urn = backStackEntry.arguments?.getString("showUrn")!!)
            }
        }
    }

    data class TvShows(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/shows"
        }

        companion object {
            const val route = "$RootRoute/{bu}/tv/shows"

            fun parse(backStackEntry: NavBackStackEntry): TvShows {
                return TvShows(backStackEntry.readBu())
            }
        }
    }

    data class TVLatestMedias(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/latestMedia"
        }

        companion object {
            const val route = "$RootRoute/{bu}/tv/latestMedia"

            fun parse(backStackEntry: NavBackStackEntry): TVLatestMedias {
                return TVLatestMedias(backStackEntry.readBu())
            }
        }
    }

    data class TVLivestreams(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/livestream"
        }

        companion object {
            const val route = "$RootRoute/{bu}/tv/livestream"

            fun parse(backStackEntry: NavBackStackEntry): TVLivestreams {
                return TVLivestreams(backStackEntry.readBu())
            }
        }
    }

    data class TVLiveCenter(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/livecenter"
        }

        companion object {
            const val route = "$RootRoute/{bu}/tv/livecenter"

            fun parse(backStackEntry: NavBackStackEntry): TVLiveCenter {
                return TVLiveCenter(backStackEntry.readBu())
            }
        }
    }

    data class TVLiveWeb(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/tv/liveweb"
        }

        companion object {
            const val route = "$RootRoute/{bu}/tv/liveweb"

            fun parse(backStackEntry: NavBackStackEntry): TVLiveWeb {
                return TVLiveWeb(backStackEntry.readBu())
            }
        }
    }

    data class RadioLiveStreams(override val bu: Bu) : ContentListWithBu {
        override fun getDestinationRoute(): String {
            return "$RootRoute/$bu/radio/livestream"
        }

        companion object {
            const val route = "$RootRoute/{bu}/radio/livestream"

            fun parse(backStackEntry: NavBackStackEntry): RadioLiveStreams {
                return RadioLiveStreams(backStackEntry.readBu())
            }
        }
    }

    data class RadioShows(val radioChannel: RadioChannel) : ContentList {
        override fun getDestinationRoute(): String {
            return "$RootRoute/${radioChannel.bu}/radio/shows/$radioChannel"
        }

        companion object {
            const val route = "$RootRoute/{bu}/radio/shows/{radioChannel}"

            fun parse(backStackEntry: NavBackStackEntry): RadioShows {
                return RadioShows(backStackEntry.readRadioChannel())
            }
        }
    }

    data class RadioLatestMedias(val radioChannel: RadioChannel) : ContentList {
        override fun getDestinationRoute(): String {
            return "$RootRoute/${radioChannel.bu}/radio/latestMedia/$radioChannel"
        }

        companion object {
            const val route = "$RootRoute/{bu}/radio/latestMedia/{radioChannel}"

            fun parse(backStackEntry: NavBackStackEntry): RadioLatestMedias {
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
