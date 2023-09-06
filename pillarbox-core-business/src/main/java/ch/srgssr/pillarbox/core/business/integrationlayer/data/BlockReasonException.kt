/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Block reason exception
 *
 * @property blockReason the reason a [Chapter] or a [Segment] is blocked.
 */
class BlockReasonException(val blockReason: BlockReason) : RuntimeException(blockReason.name)
