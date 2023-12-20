/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.LruCache
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.net.URL

/**
 * Pillarbox media description adapter
 *
 * @param pendingIntent [PendingIntent] to use when a user click the notification.
 * @param context Context of the application.
 *
 * @constructor
 */
class PillarboxMediaDescriptionAdapter(
    private val pendingIntent: PendingIntent?,
    context: Context
) : MediaDescriptionAdapter {
    private val imageMaxWidth: Int = context.resources.getDimensionPixelSize(
        androidx.media3.ui.R.dimen
            .compat_notification_large_icon_max_width
    )
    private val imageMaxHeight: Int = context.resources.getDimensionPixelSize(androidx.media3.ui.R.dimen.compat_notification_large_icon_max_height)
    private val bitmapCache = LruCache<Uri, Bitmap>(3)

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
        return when {
            player.mediaMetadata.artworkData != null -> {
                val data = player.mediaMetadata.artworkData!!
                BitmapFactory.decodeByteArray(data, /* offset= */0, data.size)
            }

            player.mediaMetadata.artworkUri != null -> {
                val imageUri = player.mediaMetadata.artworkUri!!
                val artworkBitmap = bitmapCache.get(imageUri)
                if (artworkBitmap == null) {
                    loadBitmapFromUri(imageUri, callback)
                }
                artworkBitmap // FIXME could return placeholder.
            }

            else -> {
                null // FIXME could return placeholder.
            }
        }
    }

    private fun loadBitmapFromUri(imageUri: Uri, callback: PlayerNotificationManager.BitmapCallback) {
        val imageUrl = URL(imageUri.toString())
        val opts = BitmapFactory.Options().apply {
            outWidth = imageMaxWidth
            outHeight = imageMaxHeight
        }
        runBlocking(Dispatchers.IO) {
            val result = runCatching {
                BitmapFactory.decodeStream(imageUrl.openStream(), null, opts)
            }
            result.getOrNull()?.let {
                bitmapCache.put(imageUri, it)
                callback.onBitmap(it)
            }
        }
    }
}
