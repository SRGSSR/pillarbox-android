/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.currentMediaItemAsFlow
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.player.extension.getCurrentChapters
import ch.srgssr.pillarbox.player.extension.pillarboxData
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
    val player: Player = DefaultPillarbox(context = application)

    /**
     * Chapters
     */
    val chapters: StateFlow<List<Chapter>>

    /**
     * Current chapter
     */
    val currentChapter: StateFlow<Chapter?>

    /**
     * Progress tracker
     */
    val progressTracker = SimpleProgressTrackerState(player, viewModelScope)

    init {
        chapters = player.currentMediaItemAsFlow().map {
            it?.pillarboxData?.chapters ?: emptyList()
        }.stateIn(
            viewModelScope, SharingStarted.Lazily, player.getCurrentChapters()
        )

        currentChapter = progressTracker.progress.map {
            player.getChapterAtPosition(it.inWholeMilliseconds)
        }.stateIn(viewModelScope, SharingStarted.Lazily, player.getChapterAtPosition())

        player.prepare()
        player.setMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
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
