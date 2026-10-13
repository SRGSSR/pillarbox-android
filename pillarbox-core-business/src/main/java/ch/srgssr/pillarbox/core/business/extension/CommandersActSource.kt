/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.extension

import android.os.Bundle
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSource

private const val KeyPageId = "pageId"
private const val KeyPageVersion = "pageVersion"
private const val KeySectionId = "sectionId"
private const val KeySectionVersion = "sectionVersion"
private const val KeySectionPosition = "sectionPosition"
private const val KeyItemPositionInSection = "itemPositionInSection"
private const val KeyLabels = "labels"

/**
 * Converts this [CommandersActSource] into a [Bundle] containing only platform types.
 *
 * The [Bundle] is stored in the [androidx.media3.common.MediaMetadata.extras], which are shared with other processes through the media session
 * (e.g. Bluetooth AVRCP). Those processes can't unmarshal custom [android.os.Parcelable] classes, so only platform types must be used.
 */
internal fun CommandersActSource.toBundle(): Bundle {
    return Bundle().apply {
        putString(KeyPageId, pageId)
        pageVersion?.let { putString(KeyPageVersion, it) }
        sectionId?.let { putString(KeySectionId, it) }
        sectionVersion?.let { putString(KeySectionVersion, it) }
        sectionPosition?.let { putInt(KeySectionPosition, it) }
        itemPositionInSection?.let { putInt(KeyItemPositionInSection, it) }
        labels?.let { labels ->
            putBundle(
                KeyLabels,
                Bundle().apply {
                    labels.forEach { (key, value) -> putString(key, value) }
                },
            )
        }
    }
}

/**
 * Creates a [CommandersActSource] from a [Bundle] created with [toBundle].
 *
 * @return `null` if the [Bundle] doesn't contain a page id.
 */
internal fun Bundle.toCommandersActSource(): CommandersActSource? {
    val pageId = getString(KeyPageId) ?: return null
    return CommandersActSource(
        pageId = pageId,
        pageVersion = getString(KeyPageVersion),
        sectionId = getString(KeySectionId),
        sectionVersion = getString(KeySectionVersion),
        sectionPosition = if (containsKey(KeySectionPosition)) getInt(KeySectionPosition) else null,
        itemPositionInSection = if (containsKey(KeyItemPositionInSection)) getInt(KeyItemPositionInSection) else null,
        labels = getBundle(KeyLabels)?.let { labels ->
            labels.keySet().mapNotNull { key -> labels.getString(key)?.let { key to it } }.toMap()
        },
    )
}
