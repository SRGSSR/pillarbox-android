/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Type of the media. This is different to MediaType.
 */
@Suppress("UndocumentedPublicProperty")
enum class Type {
    EPISODE, EXTRACT, TRAILER, CLIP, LIVESTREAM, SCHEDULED_LIVESTREAM
}
