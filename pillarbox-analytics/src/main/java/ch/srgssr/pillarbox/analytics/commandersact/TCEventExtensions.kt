/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import com.tagcommander.lib.serverside.events.base.TCEvent

/**
 * Adds a parameter to this [TCEvent] if the provided data is not blank.
 *
 * @param key The key of the parameter.
 * @param data The data of the parameter. If this value is `null` or blank, the parameter will not be added.
 */
internal fun TCEvent.addAdditionalParameterIfNotBlank(key: String, data: String?) {
    if (!data.isNullOrBlank()) {
        addAdditionalProperty(key, data)
    }
}
