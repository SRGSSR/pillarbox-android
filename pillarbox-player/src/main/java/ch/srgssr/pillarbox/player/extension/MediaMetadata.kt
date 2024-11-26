/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

/**
 * A list of [Chapter]s for this media item.
 *
 * @return A list of [Chapter]s for this media item, or `null` if there are no chapters.
 */
val MediaMetadata.chapters: List<Chapter>?
    get() = getExtra(KeyChapters)

/**
 * Sets the [Chapter]s.
 *
 * **Note:** calling [MediaMetadata.Builder.setExtras] after this function will reset the chapters information.
 *
 * @param chapters The list of [Chapter]s.
 * @return This [MediaMetadata.Builder] instance for method chaining.
 */
fun MediaMetadata.Builder.setChapters(chapters: List<Chapter>): MediaMetadata.Builder {
    return setExtra(KeyChapters, chapters)
}

/**
 * A list of [Credit]s for this media item.
 *
 * @return A list of [Credit]s for this media item, or `null` if there are no credits.
 */
val MediaMetadata.credits: List<Credit>?
    get() = getExtra(KeyCredits)

/**
 * Sets the [Credit]s.
 *
 * **Note:** calling [MediaMetadata.Builder.setExtras] after this function will reset the credits information.
 *
 * @param credits The list of [Credit]s.
 * @return This [MediaMetadata.Builder] instance for method chaining.
 */
fun MediaMetadata.Builder.setCredits(credits: List<Credit>): MediaMetadata.Builder {
    return setExtra(KeyCredits, credits)
}

private inline fun <reified T : Parcelable> MediaMetadata.getExtra(key: String): List<T>? {
    return extras?.let { BundleCompat.getParcelableArrayList(it, key, T::class.java) }
}

private fun <T : Parcelable> MediaMetadata.Builder.setExtra(key: String, items: List<T>): MediaMetadata.Builder {
    val extras = build().extras ?: Bundle()
    extras.putParcelableArrayList(key, ArrayList(items))
    return setExtras(extras)
}

private const val KeyCredits = "Pillarbox-credits"
private const val KeyChapters = "Pillarbox-chapters"
