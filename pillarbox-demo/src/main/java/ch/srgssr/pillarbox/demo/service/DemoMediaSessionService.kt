/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.di.Dependencies
import ch.srgssr.pillarbox.demo.ui.player.mediacontroller.MediaControllerActivity
import ch.srgssr.pillarbox.player.service.PillarboxMediaSessionService
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * Demo media session service to handle background playback has Media3 would us to use.
 * Can be still useful when using with MediaLibrary for android auto.
 *
 * Limitations :
 *  - No custom data access from MediaController so no MediaComposition or other custom attributes integrator wants.
 *
 * @constructor Create empty Demo media session service
 */
class DemoMediaSessionService : PillarboxMediaSessionService() {

    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val player = Dependencies.provideDefaultPlayer(this)
        // TODO add item elsewhere
        player.setMediaItems(
            listOf(
                MediaItem.Builder().setMediaId("urn:rts:video:6820736").build(),
                MediaItem.Builder().setMediaId("urn:rts:video:8393241").build(),
                DemoItem(title = "Swiss cheese fondue", uri = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8").toMediaItem(),
                MediaItem.Builder().setMediaId("urn:rts:video:3608506").build(),
            )
        )
        player.setWakeMode(C.WAKE_MODE_NETWORK)
        player.setHandleAudioFocus(true)
        player.prepare()
        player.play()

        setPlayer(player)
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
