/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import kotlinx.serialization.Serializable

/**
 * The custom data.
 * @property blockingReason The blocking reason.
 */
@Serializable
data class CustomData(val blockingReason: String? = null)
