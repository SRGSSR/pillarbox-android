/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

/**
 * Represents the different host URLs for the integration layer service.
 *
 * @property baseHostUrl The base URL of the environment.
 */
enum class IlHost(val baseHostUrl: String) {

    /**
     * The base URL for the production environment.
     */
    PROD(baseHostUrl = "https://il.srgssr.ch"),

    /**
     * The base URL for the test environment.
     */
    TEST(baseHostUrl = "https://il-test.srgssr.ch"),

    /**
     * The base URL for the stage environment.
     */
    STAGE(baseHostUrl = "https://il-stage.srgssr.ch"),

    ;

    companion object {

        /**
         * Parses the given [url] and returns the corresponding [IlHost].
         *
         * @param url The URL to parse.
         *
         * @return The matching [IlHost] or `null` if none was found.
         */
        fun parse(url: String): IlHost? {
            return entries.find { url.startsWith(it.baseHostUrl) }
        }
    }
}
