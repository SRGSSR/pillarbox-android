/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.media.maestro.MediaRouteButton
import ch.srgssr.pillarbox.cast.RemotePlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.currentPillarboxMetadataAsFlow
import ch.srgssr.pillarbox.ui.SimpleProgressTrackerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Chapters showcase view model
 *
 * @param application Application.
 */
class ChaptersShowcaseViewModel(application: Application) : AndroidViewModel(application) {
    private val castContext = application.getCastContext()

    private val castPlayer = PillarboxCastPlayer(application)
    private val localPlayer = PillarboxExoPlayer(application)

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val player = RemotePlayer(localPlayer = localPlayer, castPlayer = castPlayer)

    /**
     * The media to play.
     */
    val demoItem = SamplesSRG.OnDemandHorizontalVideo

    /**
     * Progress tracker
     */
    val progressTracker = SimpleProgressTrackerState(player, viewModelScope)

    /**
     * Chapters
     */
    val chapters: StateFlow<List<Chapter>> = player.currentPillarboxMetadataAsFlow()
        .map { it.chapters }
        .stateIn(viewModelScope, SharingStarted.Lazily, player.currentPillarboxMetadata.chapters)

    /**
     * Current chapter
     */
    val currentChapter: StateFlow<Chapter?> = progressTracker.progress
        .map { player.getChapterAtPosition(it.inWholeMilliseconds) }
        .stateIn(viewModelScope, SharingStarted.Lazily, player.getChapterAtPosition())

    /**
     * The [MediaRouteSelector] to use on the [MediaRouteButton].
     */
    val routeSelector = castContext.mergedSelector ?: MediaRouteSelector.Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()

    init {
        player.prepare()
        player.setMediaItem(demoItem.toMediaItem())
    }

    /**
     * Chapter clicked
     *
     * @param chapter
     */
    fun chapterClicked(chapter: Chapter) {
        player.seekTo(chapter.start)
    }

    override fun onCleared() {
        player.release()
    }
}
