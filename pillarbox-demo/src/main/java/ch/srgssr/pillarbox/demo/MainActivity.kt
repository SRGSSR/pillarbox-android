/*
 * Copyright (c) 2022-2022.  SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.srgssr.pillarbox.analytics.Analytics
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Copyright (c) SRG SSR. All rights reserved.
 * <p>
 * License information is available from the LICENSE file.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var versionView: TextView
    private val analytics = Analytics()
    private val player = PillarboxPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        versionView = findViewById(R.id.version_view)
        versionView.text = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        analytics.hello()
        player.hello()
    }
}
