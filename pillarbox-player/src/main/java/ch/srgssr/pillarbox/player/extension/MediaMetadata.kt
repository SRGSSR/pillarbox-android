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
 * Chapters
 */
val MediaMetadata.chapters: List<Chapter>?
    get() {
        return extras?.let { BundleCompat.getParcelableArrayList(it, KeyChapters, Chapter::class.java) }
    }

/**
 * Sets the [MediaMetadata.chapters].
 * Calling [MediaMetadata.Builder.setExtras] after will reset this call.
 * @param chapters The list of [Chapter].
 */
fun MediaMetadata.Builder.setChapters(chapters: List<Chapter>): MediaMetadata.Builder {
    return setExtras(KeyChapters, chapters)
}

/**
 * Credits
 */
val MediaMetadata.credits: List<Credit>?
    get() {
        return extras?.let { BundleCompat.getParcelableArrayList(it, KeyCredits, Credit::class.java) }
    }

/**
 * Sets the [MediaMetadata.credits]
 * Calling [MediaMetadata.Builder.setExtras] after will reset this call.
 * @param credits The list of [Credit].
 */
fun MediaMetadata.Builder.setCredits(credits: List<Credit>): MediaMetadata.Builder {
    return setExtras(KeyCredits, credits)
}

private fun <T : Parcelable> MediaMetadata.Builder.setExtras(key: String, items: List<T>): MediaMetadata.Builder {
    val extras = build().extras ?: Bundle()
    extras.putParcelableArrayList(key, ArrayList(items))
    return setExtras(extras)
}

private const val KeyCredits = "Pillarbox-credits"
private const val KeyChapters = "Pillarbox-chapters"
