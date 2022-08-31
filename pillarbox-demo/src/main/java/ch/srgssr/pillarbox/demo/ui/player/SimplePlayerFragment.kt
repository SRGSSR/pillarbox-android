/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.demo.R

/**
 * Simple player fragment
 *
 * @constructor Create empty Simple player fragment
 */
class SimplePlayerFragment : Fragment() {
    private lateinit var playerView: PlayerView
    private val playerViewModel: SimplePlayerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_simple_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerView = view.findViewById(R.id.player_view)
        val buttonStop: Button = view.findViewById(R.id.button_stop)
        val buttonPrepare: Button = view.findViewById(R.id.button_prepare)
        val buttonRelease: Button = view.findViewById(R.id.button_release)

        buttonStop.setOnClickListener { playerViewModel.player.stop() }
        buttonPrepare.setOnClickListener { playerViewModel.player.prepare() }
        buttonRelease.setOnClickListener { playerViewModel.player.release() }
    }

    override fun onStart() {
        super.onStart()
        playerView.onResume()
        playerViewModel.resumePlayback()
        playerView.player = playerViewModel.player
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.pausePlayback()
        playerView.onPause()
        playerView.player = null
    }
}
