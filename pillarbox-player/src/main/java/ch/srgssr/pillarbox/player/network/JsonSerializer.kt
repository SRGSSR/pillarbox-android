/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import androidx.annotation.RestrictTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json

/**
 * The [Json] serializer used for Pillarbox network requests.
 */
@OptIn(ExperimentalSerializationApi::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val jsonSerializer = Json {
    classDiscriminatorMode = ClassDiscriminatorMode.NONE
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
    isLenient = true
}
