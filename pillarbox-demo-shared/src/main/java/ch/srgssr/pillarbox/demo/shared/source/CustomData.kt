/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import kotlinx.serialization.Serializable

@Serializable
data class CustomData(val blockingReason: String? = null)
