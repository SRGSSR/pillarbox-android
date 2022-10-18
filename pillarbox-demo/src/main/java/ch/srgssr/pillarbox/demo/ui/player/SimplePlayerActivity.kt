/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.data.DemoItem

/**
 * Simple player activity using a SimplePlayerFragment
 *
 * @constructor Create empty Simple player activity
 */
class SimplePlayerActivity : FragmentActivity() {

    private val playerViewModel: SimplePlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_player)
        if (savedInstanceState == null) {
            intent.extras?.let {
                val ids: Array<String> = it.getStringArray(MEDIA_IDS)!!
                playerViewModel.playItemIds(ids)
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
