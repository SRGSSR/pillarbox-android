/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import ch.srgssr.pillarbox.player.network.jsonSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json

/**
 * [Json] to use when serializing [TimeRange].
 */
@OptIn(ExperimentalSerializationApi::class)
val jsonTimeRanges = Json(jsonSerializer) {
    classDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC
}
