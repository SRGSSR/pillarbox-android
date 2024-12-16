/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

/**
 * Object containing the different host URLs for the integration layer service.
 * @property baseHostUrl The base hostname url.
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

    @Suppress("UndocumentedPublicClass")
    companion object {

        /**
         * Parses the given [url] and returns the corresponding [IlHost].
         *
         * @param url The url to parse
         * @return null if the [url] does not match any [IlHost].
         */
        fun parse(url: String): IlHost? {
            return entries.find { url.contains(it.baseHostUrl) }
        }
    }
}
