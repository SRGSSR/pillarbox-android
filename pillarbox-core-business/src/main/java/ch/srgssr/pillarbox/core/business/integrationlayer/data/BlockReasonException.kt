/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Block reason exception
 *
 * @property blockReason the reason a [Chapter] is blocked.
 */
class BlockReasonException(val blockReason: String) : RuntimeException(blockReason)
