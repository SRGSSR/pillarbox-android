/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Simple player activity using a SimplePlayerFragment
 *
 * @constructor Create empty Simple player activity
 */
class SimplePlayerActivity : ComponentActivity() {

    private val playerViewModel: SimplePlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            intent.extras?.let {
                val ids: Array<String> = it.getStringArray(MEDIA_IDS)!!
                playerViewModel.playItemIds(ids)
            }
        }
        setContent {
            PillarboxTheme {
                DemoPlayerView(playerViewModel = playerViewModel)
            }
        }
    }

    companion object {
        private const val MEDIA_IDS = "ARG_MEDIAS"

        /**
         * Start activity [SimplePlayerActivity] with [demoItem]
         */
        fun startActivity(context: Context, demoItem: DemoItem) {
            val intent = Intent(context, SimplePlayerActivity::class.java)
            intent.putExtra(MEDIA_IDS, arrayOf(demoItem.id))
            context.startActivity(intent)
        }
    }
}
