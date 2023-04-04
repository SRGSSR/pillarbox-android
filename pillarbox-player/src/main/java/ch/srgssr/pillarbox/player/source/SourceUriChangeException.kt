/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.MediaItem
import java.io.IOException

/**
 * Source uri change exception
 *
 * @property mediaItem before the exception.
 * @property updatedMediaItem the media item that trigger the exception.
 * @constructor Create empty Source uri change exception
 */
class SourceUriChangeException(val mediaItem: MediaItem, val updatedMediaItem: MediaItem) : IOException()
