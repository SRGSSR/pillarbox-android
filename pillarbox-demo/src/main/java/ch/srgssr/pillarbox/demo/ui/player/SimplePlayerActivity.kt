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
            val ids: Array<String> = intent.extras?.getStringArray(EXTRA_IDS) ?: emptyArray()
            playerViewModel.playItemIds(ids)
        }
    }

    companion object {
        private const val EXTRA_IDS = "EXTRA_IDS"

        /**
         * Start player Activity with given [ids] to play
         *
         * @param context
         * @param ids
         */
        fun startPlayer(context: Context, ids: Array<String>) {
            val intent = Intent(context, SimplePlayerActivity::class.java)
            intent.putExtra(EXTRA_IDS, ids)
            context.startActivity(intent)
        }
    }
}
