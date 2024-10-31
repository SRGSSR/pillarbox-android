/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts.thumbnail

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.exoplayer.image.ImageOutput
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.SimpleProgressTrackerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * A ViewModel to demonstrate how to work with Image track.
 *
 * @param application The [Application].
 */
class ThumbnailViewModel(application: Application) : AndroidViewModel(application), ImageOutput {
    /**
     * Player
     */
    val player = PillarboxExoPlayer(application)
    private val _thumbnail = mutableStateOf<Bitmap?>(null)

    /**
     * Thumbnail
     */
    val thumbnail: State<Bitmap?> = _thumbnail

    /**
     * Progress tracker state
     */
    val progressTrackerState: ProgressTrackerState = ThumbnailProgressTracker(player, viewModelScope, this)

    init {
        player.prepare()
        player.addMediaItem(SRGMediaItem("urn:srf:video:881be9c2-65ec-4fa9-ba4a-926d15d046ef"))
        player.addMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
        player.addMediaItem(DemoItem.UnifiedStreamingOnDemand_Dash_TiledThumbnails.toMediaItem())
        player.addMediaItem(DemoItem.UnifiedStreamingOnDemand_Dash_TrickPlay.toMediaItem())
    }

    override fun onCleared() {
        player.release()
    }

    override fun onImageAvailable(presentationTimeUs: Long, bitmap: Bitmap) {
        _thumbnail.value = bitmap
    }

    override fun onDisabled() {
        _thumbnail.value = null
    }

    /**
     * Thumbnail progress tracker
     * FIXME create a real progress tracker in pillarbox-ui
     */
    internal class ThumbnailProgressTracker(
        private val player: PillarboxExoPlayer,
        coroutineScope: CoroutineScope,
        private val imageOutput: ImageOutput,
    ) : ProgressTrackerState {
        private var storedSeekParameters = player.seekParameters
        private var storedPlayWhenReady = player.playWhenReady
        private var storedSmoothSeeking = player.smoothSeekingEnabled
        private var storedTrackSelectionParameters = player.trackSelectionParameters
        private val simpleProgressTrackerState = SimpleProgressTrackerState(player, coroutineScope)
        private var startChanging = false
        override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

        override fun onChanged(progress: Duration) {
            simpleProgressTrackerState.onChanged(progress)
            if (!startChanging) {
                startChanging = true
                storedPlayWhenReady = player.playWhenReady
                storedSmoothSeeking = player.smoothSeekingEnabled
                storedSeekParameters = player.seekParameters
                storedTrackSelectionParameters = player.trackSelectionParameters
                player.smoothSeekingEnabled = true
                player.playWhenReady = false
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setPreferredVideoRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                    .setTrackTypeDisabled(C.TRACK_TYPE_METADATA, true)
                    .setPrioritizeImageOverVideoEnabled(true)
                    .build()
                player.setImageOutput(imageOutput)
            }
            player.seekTo(progress.inWholeMilliseconds)
        }

        override fun onFinished() {
            startChanging = false
            simpleProgressTrackerState.onFinished()
            player.trackSelectionParameters = storedTrackSelectionParameters
            player.smoothSeekingEnabled = storedSmoothSeeking
            player.setSeekParameters(storedSeekParameters)
            player.playWhenReady = storedPlayWhenReady
            player.setImageOutput(null)
        }
    }
}
