/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.srgssr.pillarbox.demo.player.SimplePlayerActivity

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var versionView: TextView
    private lateinit var startDemoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        versionView = findViewById(R.id.version_view)
        versionView.text = BuildConfig.VERSION_NAME
        startDemoButton = findViewById(R.id.button_start_demo)
        startDemoButton.setOnClickListener {
            val playerIntent = Intent(this, SimplePlayerActivity::class.java)
            startActivity(playerIntent)
            finish()
        }
    }
}
