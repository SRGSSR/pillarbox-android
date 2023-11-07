package ch.srgssr.pillarbox.core.business.integrationlayer.service

import java.net.URL

/**
 * Copyright (c) SRG SSR. All rights reserved.
 *
 *
 * License information is available from the LICENSE file.
 */
object IlHost {
    /**
     * Prod host url
     */
    val PROD = URL("https://il.srgssr.ch/")

    /**
     * Test host url
     */
    val TEST = URL("https://il-test.srgssr.ch/")

    /**
     * Stage host url
     */
    val STAGE = URL("https://il-stage.srgssr.ch/")

    /**
     * Default host to use throughout the library by default.
     */
    val DEFAULT = PROD
}
