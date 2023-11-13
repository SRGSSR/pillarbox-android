/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.leanback

import android.os.Bundle
import android.util.Log
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackSeekDataProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import ch.srgssr.pillarbox.core.business.SRGErrorMessageProvider
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.currentMediaMetadataAsFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val UpdateInterval = 1_000

/**
 * Leanback player fragment
 *
 * A simple leanback player sample.
 * Lot of work is still needed to have a good player experience.
 */
class LeanbackPlayerFragment : VideoSupportFragment() {
    private lateinit var player: PillarboxPlayer

    /**
     * Set demo item to [PillarboxPlayer]
     *
     * @param demoItem
     */
    fun setDemoItem(demoItem: DemoItem) {
        player.setMediaItem(demoItem.toMediaItem())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = PlayerModule.provideDefaultPlayer(requireContext()).apply {
            prepare()
            setHandleAudioFocus(true)
        }
        val playerGlue = PlaybackTransportControlGlue(
            requireActivity(),
            LeanbackPlayerAdapter(
                requireActivity(),
                player,
                UpdateInterval
            ).apply {
                setErrorMessageProvider(SRGErrorMessageProvider(requireContext()))
            }
        )
        playerGlue.host = VideoSupportFragmentGlueHost(this)
        playerGlue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
            override fun onPreparedStateChanged(glue: PlaybackGlue) {
                if (glue.isPrepared) {
                    playerGlue.seekProvider = PlaybackSeekDataProvider()
                    playerGlue.play()
                }
            }
        })
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                player.currentMediaMetadataAsFlow().flowWithLifecycle(lifecycle).collectLatest {
                    playerGlue.subtitle = it.title
                    playerGlue.title = it.subtitle
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (player.playerError == null || player.playbackState == Player.STATE_ENDED) {
            player.seekToDefaultPosition()
            player.prepare()
        }
        player.play()
    }

    override fun onPause() {
        Log.d("Coucou", "PlayerFragment:onPause")
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Coucou", "PlayerFragment:onDestroy")
        player.release()
    }
}
