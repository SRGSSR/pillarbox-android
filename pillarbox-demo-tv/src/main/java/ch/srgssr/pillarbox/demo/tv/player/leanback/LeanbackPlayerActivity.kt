/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.leanback

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.tv.R

/**
 * Player activity
 *
 * @constructor Create empty Player activity
 */
class LeanbackPlayerActivity : FragmentActivity() {

    private lateinit var leanbackPlayerFragment: LeanbackPlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        leanbackPlayerFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as LeanbackPlayerFragment
        val demoItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(ARG_ITEM, DemoItem::class.java)
        } else {
            intent.getSerializableExtra(ARG_ITEM) as DemoItem?
        }
        demoItem?.let {
            leanbackPlayerFragment.setDemoItem(it)
        }
    }

    companion object {
        private const val ARG_ITEM = "demo_item"

        /**
         * Start player with Leanback fragment.
         *
         * @param context
         * @param demoItem The item to play.
         */
        fun startPlayer(context: Activity, demoItem: DemoItem) {
            val intent = Intent(context, LeanbackPlayerActivity::class.java)
            intent.putExtra(ARG_ITEM, demoItem)
            context.startActivity(intent)
        }
    }
}
