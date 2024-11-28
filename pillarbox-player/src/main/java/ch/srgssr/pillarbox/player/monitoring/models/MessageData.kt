/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import kotlinx.serialization.Serializable

/**
 * Represents the base interface for all data carried by monitoring messages.
 */
@Serializable
sealed interface MessageData
