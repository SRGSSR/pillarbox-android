/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.analytics.Analytics
import ch.srgssr.pillarbox.demo.data.SwiMediaItemSource
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var versionView: TextView
    private val analytics = Analytics()
    private lateinit var player: PillarboxPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        versionView = findViewById(R.id.version_view)
        versionView.text = BuildConfig.VERSION_NAME
        player = PillarboxPlayer(this, SwiMediaItemSource())
        player.setMediaItem(
            MediaItem.Builder().setMediaId(SwiMediaItemSource.UNIQUE_SWI_ID).build()
        )
        player.prepare()
    }

    override fun onResume() {
        super.onResume()
        analytics.hello()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }
}
