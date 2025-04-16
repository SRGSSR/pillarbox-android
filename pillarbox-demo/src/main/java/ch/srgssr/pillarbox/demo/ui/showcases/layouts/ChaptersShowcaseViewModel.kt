/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.currentMediaMetadataAsFlow
import ch.srgssr.pillarbox.player.extension.chapters
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.player.extension.getCurrentChapters
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
    /**
     * Player
     */
    val player: Player = PillarboxExoPlayer(application)

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
    val chapters: StateFlow<List<Chapter>> = player.currentMediaMetadataAsFlow()
        .map { it.chapters.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, player.getCurrentChapters())

    /**
     * Current chapter
     */
    val currentChapter: StateFlow<Chapter?> = progressTracker.progress
        .map { player.getChapterAtPosition(it.inWholeMilliseconds) }
        .stateIn(viewModelScope, SharingStarted.Lazily, player.getChapterAtPosition())

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
