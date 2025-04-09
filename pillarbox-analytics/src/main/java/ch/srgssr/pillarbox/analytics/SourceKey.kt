/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * CommandersAct source key.
 *
 * @property key the CommandersAct source key.
 */
enum class SourceKey(val key: String) {
    /**
     * The source key for SRG SSR apps in production.
     */
    PRODUCTION("3909d826-0845-40cc-a69a-6cec1036a45c"),

    /**
     * The source key for SRG SSR apps in development.
     */
    DEVELOPMENT("6f6bf70e-4129-4e47-a9be-ccd1737ba35f"),
}
