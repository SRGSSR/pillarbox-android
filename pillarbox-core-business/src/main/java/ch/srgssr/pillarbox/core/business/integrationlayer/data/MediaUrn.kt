/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import java.util.regex.Pattern

/**
 * This object provides functionality to validate whether a given [String] is a valid media URN.
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
     * Checks if the given [String] is a valid media URN.
     *
     * @param urn The [String] to be checked.
     * @return `true` if the given [String] is a valid media URN, `false` otherwise.
     */
    fun isValid(urn: String): Boolean {
        return pattern.matcher(urn).matches()
    }
}

/**
 * Checks if [this] [String] is a valid media URN.
 *
 * @return `true` if [this] [String] is a valid media URN, `false` otherwise.
 */
fun String?.isValidMediaUrn(): Boolean {
    return this?.let { MediaUrn.isValid(it) } ?: false
}
