/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import com.tagcommander.lib.serverside.events.base.TCEvent

/**
 * CommandersAct event conversion
 */
object TCEventExtensions {

    /**
     * Add additional parameter if not blank
     *
     * @param key Key to add data.
     * @param data Data to add if not null or blank.
     */
    fun TCEvent.addAdditionalParameterIfNotBlank(key: String, data: String?) {
        if (!data.isNullOrBlank()) {
            addAdditionalProperty(key, data)
        }
    }
}
