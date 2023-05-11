/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.BuildConfig

/**
 * Config
 *
 * @property virtualSite The app site name given by the analytics team.
 * @property sourceKey The sourceKey given by the analytics team.
 */
data class CommandersActConfig(
    val virtualSite: String,
    val sourceKey: String = if (BuildConfig.DEBUG) SOURCE_KEY_SRG_DEBUG else SOURCE_KEY_SRG_PROD
) {

    companion object {
        /**
         * SRG Production CommandersAct configuration
         */
        const val SOURCE_KEY_SRG_PROD = "3909d826-0845-40cc-a69a-6cec1036a45c"

        /**
         * SRG Debug CommandersAct configuration
         */
        const val SOURCE_KEY_SRG_DEBUG = "6f6bf70e-4129-4e47-a9be-ccd1737ba35f"
    }
}
