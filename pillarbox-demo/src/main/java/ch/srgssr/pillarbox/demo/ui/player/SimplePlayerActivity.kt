/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

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
            intent.extras?.let {
                val args = SimplePlayerActivityArgs.fromBundle(it)
                val ids: Array<String> = args.arrayMediaId
                playerViewModel.playItemIds(ids)
            }
        }
    }
}
