/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import java.util.regex.Pattern

/**
 * Media urn
 */
object MediaUrn {

    /**
     * Pattern used by Integration layer to identify a media urn
     */
    private const val URN_MEDIA_REGEX = "urn:(srf|rtr|rts|rsi|swi|swisstxt)(:(livestream|scheduled_livestream))?(:ssatr)?:" +
        "(video|video:clip|audio)(:" +
        "(srf|rtr|rts|rsi|swi))?:((\\d+|[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}|[0-9a-fA-F]{8}-" +
        "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}_program_\\d+|livestream_\\S*|[0-9a-z\\-]+(_\\w+)?)" +
        "|(rsc-de|rsc-fr|rsc-it|rsj|rsp))"

    private val pattern = Pattern.compile(URN_MEDIA_REGEX)

    /**
     * @param urn
     * @return true if it is a valid media urn
     */
    fun isValid(urn: String): Boolean {
        return pattern.matcher(urn).matches()
    }
}

/**
 * Check
 *
 * @return
 */
fun String?.isValidMediaUrn(): Boolean {
    return this?.let { MediaUrn.isValid(it) } ?: false
}
