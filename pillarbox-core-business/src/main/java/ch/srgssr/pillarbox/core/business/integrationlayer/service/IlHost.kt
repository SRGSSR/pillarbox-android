/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import java.net.URL

/**
 * Object containing the different host URLs for the integration layer service.
 */
object IlHost {
    /**
     * The base URL for the production environment.
     */
    val PROD = URL("https://il.srgssr.ch/")

    /**
     * The base URL for the test environment.
     */
    val TEST = URL("https://il-test.srgssr.ch/")

    /**
     * The base URL for the stage environment.
     */
    val STAGE = URL("https://il-stage.srgssr.ch/")

    /**
     * The default host used by the library.
     */
    val DEFAULT = PROD
}
