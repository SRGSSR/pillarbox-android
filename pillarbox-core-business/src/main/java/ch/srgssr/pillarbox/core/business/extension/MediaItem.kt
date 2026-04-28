/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.extension

import androidx.core.os.BundleCompat
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSource
import ch.srgssr.pillarbox.core.business.SRGMediaItemBuilder.Companion.EXTRAS_KEY_COMMANDERS_ACT_SOURCE

/**
 * The [CommandersActSource] if it has any.
 */
val MediaItem.commandersActSource: CommandersActSource?
    get() {
        return mediaMetadata.extras?.let { BundleCompat.getParcelable(it, EXTRAS_KEY_COMMANDERS_ACT_SOURCE, CommandersActSource::class.java) }
    }
