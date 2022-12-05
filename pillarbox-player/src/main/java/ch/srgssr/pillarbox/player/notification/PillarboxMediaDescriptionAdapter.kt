/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * Pillarbox media description adapter that handle image loading with Glide.
 *
 * @property context
 * @property pendingIntent
 */
class PillarboxMediaDescriptionAdapter(private val context: Context, private val pendingIntent: PendingIntent?) : MediaDescriptionAdapter {
    private val imageMaxWidth: Int = context.resources.getDimensionPixelSize(
        androidx.media3.ui.R.dimen
            .compat_notification_large_icon_max_width
    )
    private val imageMaxHeight: Int = context.resources.getDimensionPixelSize(androidx.media3.ui.R.dimen.compat_notification_large_icon_max_height)

    override fun getCurrentContentTitle(player: Player): CharSequence {
        val displayTitle = player.mediaMetadata.displayTitle
        if (!TextUtils.isEmpty(displayTitle)) {
            return displayTitle!!
        }

        val title = player.mediaMetadata.title
        return title ?: ""
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return pendingIntent
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        val subtitle = player.mediaMetadata.subtitle
        return if (!TextUtils.isEmpty(subtitle)) {
            subtitle
        } else player.mediaMetadata.station
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        return if (player.mediaMetadata.artworkData != null) {
            val data = player.mediaMetadata.artworkData!!
            BitmapFactory.decodeByteArray(data, /* offset= */0, data.size)
        } else if (player.mediaMetadata.artworkUri != null) {
            val imageUri = player.mediaMetadata.artworkUri!!
            Glide.with(context).asBitmap()
                .load(imageUri)
                .into(object : CustomTarget<Bitmap>(imageMaxWidth, imageMaxHeight) {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Do nothing
                    }
                })
            // TODO return Bitmap placeholder while loading
            null
        } else {
            null
        }
    }
}
