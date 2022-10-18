/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.ui.PlayerView
import ch.srg.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srg.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import ch.srgssr.pillarbox.demo.R
import retrofit2.HttpException

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
    }

    override fun onStart() {
        super.onStart()
        playerView.onResume()
        playerViewModel.resumePlayback()
        playerView.player = playerViewModel.player
        playerView.setErrorMessageProvider { throwable ->
            when (val cause = throwable.cause) {
                is BlockReasonException -> {
                    Pair.create(0, cause.blockReason)
                }
                is HttpException -> {
                    Pair.create(cause.code(), cause.message)
                }
                is ResourceNotFoundException -> {
                    Pair.create(0, "Can't find Resource to play")
                }
                else -> {
                    Pair.create(throwable.errorCode, "${throwable.localizedMessage} (${throwable.errorCodeName})")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.pausePlayback()
        playerView.onPause()
        playerView.player = null
    }
}
