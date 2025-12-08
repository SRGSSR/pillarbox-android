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
import android.util.LruCache
import androidx.core.util.TypedValueCompat
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.URL

/**
 * A [MediaDescriptionAdapter] for Pillarbox.
 *
 * @param pendingIntent The [PendingIntent] to use when the notification is clicked.
 * @param context The [Context] of the application.
 * @param coroutineScope The [CoroutineScope] used for loading artwork asynchronously.
 */
class PillarboxMediaDescriptionAdapter(
    private val pendingIntent: PendingIntent?,
    context: Context,
    private val coroutineScope: CoroutineScope = MainScope()
) : MediaDescriptionAdapter {
    // Hard-code the value of compat_notification_large_icon_max_width and
    // compat_notification_large_icon_max_width as 320dp because the resource IDs are not public
    // in
    // https://cs.android.com/android/platform/superproject/+/androidx-main:frameworks/support/core/core/src/main/res/values/dimens.xml
    private val imageMaxWidth: Int = TypedValueCompat.dpToPx(NOTIFICATION_SIZE_DP.toFloat(), context.resources.displayMetrics).toInt()

    private val imageMaxHeight: Int = imageMaxWidth

    private val bitmapCache = LruCache<Uri, Bitmap>(3)

    override fun getCurrentContentTitle(player: Player): CharSequence {
        val displayTitle = player.mediaMetadata.displayTitle
        return if (!displayTitle.isNullOrEmpty()) {
            displayTitle
        } else {
            player.mediaMetadata.title ?: ""
        }
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return pendingIntent
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        val subtitle = player.mediaMetadata.subtitle
        return if (!subtitle.isNullOrEmpty()) {
            subtitle
        } else {
            player.mediaMetadata.station
        }
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
                    coroutineScope.launch(Dispatchers.IO) {
                        loadBitmapFromUri(imageUri, callback)
                    }
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
        val result = runCatching {
            imageUrl.openStream().use {
                BitmapFactory.decodeStream(it, null, opts)
            }
        }
        result.getOrNull()?.let {
            bitmapCache.put(imageUri, it)
            callback.onBitmap(it)
        }
    }
    companion object {
        private const val NOTIFICATION_SIZE_DP = 320
    }
}
