/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.media3.cast.SessionAvailabilityListener
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.widget.CastButton
import ch.srgssr.pillarbox.core.business.cast.SRGMediaItemConverter
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView

/**
 * Activity showing how to use Cast with Pillarbox.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val player = remember {
                PillarboxCastPlayer(castContext = getCastContext(), context = this, mediaItemConverter = SRGMediaItemConverter()).apply {
                    @Suppress("MaximumLineLength", "MaxLineLength")
                    val mediaItems = listOf(
                        DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML,
                        DemoItem.GoogleDashH265_CENC_Widewine,
                        DemoItem.OnDemandAudio,
                        DemoItem.OnDemandAudioMP3,
                        DemoItem.OnDemandHorizontalVideo,
                        DemoItem.DvrVideo,
                    ).map { it.toMediaItem() }

                    setSessionAvailabilityListener(object : SessionAvailabilityListener {
                        override fun onCastSessionAvailable() {
                            setMediaItems(mediaItems)
                        }

                        override fun onCastSessionUnavailable() {
                            release()
                        }
                    })
                }
            }

            PillarboxTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        CastButton()
                    },
                ) { innerPadding ->
                    ExoPlayerView(
                        player = player,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        setupView = {
                            setShowShuffleButton(true)
                            setShowSubtitleButton(true)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainPreview() {
    PillarboxTheme {
        CastButton()
    }
}
