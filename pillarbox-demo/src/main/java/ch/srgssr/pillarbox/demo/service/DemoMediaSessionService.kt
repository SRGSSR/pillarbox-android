/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.common.C
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.showcases.integrations.MediaControllerActivity
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.session.PillarboxMediaSessionService
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * Demo media session service to handle background playback has Media3 would us to use.
 * Can be still useful when using with MediaLibrary for android auto.
 *
 * Limitations:
 *  - No custom data access from MediaController so no MediaComposition or other custom attributes integrator wants.
 *
 * @constructor Create empty Demo media session service
 */
class DemoMediaSessionService : PillarboxMediaSessionService() {

    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val player = PlayerModule.provideDefaultPlayer(this)
        player.setWakeMode(C.WAKE_MODE_NETWORK)
        player.setHandleAudioFocus(true)
        player.prepare()
        player.play()

        setPlayer(player = player, sessionId = "DemoMediaSession")
    }

    override fun sessionActivity(): PendingIntent {
        val intent = Intent(applicationContext, MediaControllerActivity::class.java)
        val flags = PendingIntentUtils.appendImmutableFlagIfNeeded(PendingIntent.FLAG_UPDATE_CURRENT)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            flags
        )
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BackgroundService"
    }
}
