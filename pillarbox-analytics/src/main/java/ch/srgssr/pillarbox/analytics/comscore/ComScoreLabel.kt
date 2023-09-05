/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

internal enum class ComScoreLabel(val label: String) {
    C8("c8"),
    MP_BRAND("mp_brand"),
    MP_V("mp_v"),
    /**
     * Please refer to ComScore android implementation guide section 2.5.
     */
    USER_CONSENT("cs_ucfr"),
}
