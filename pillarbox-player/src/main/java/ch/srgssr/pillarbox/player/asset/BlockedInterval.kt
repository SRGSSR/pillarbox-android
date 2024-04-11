/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

/**
 * Blocked section
 *
 * @property id
 * @property start
 * @property end
 * @property reason
 * @constructor Create empty Blocked section
 */
data class BlockedInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val reason: String
) : TimeInterval
