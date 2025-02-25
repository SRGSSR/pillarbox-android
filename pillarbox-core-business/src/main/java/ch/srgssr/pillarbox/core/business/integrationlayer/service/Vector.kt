/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.content.Context
import android.content.res.Configuration

/**
 * Represents a vector used to distinguish between different device types.
 *
 * @param label The label of this vector.
 */
enum class Vector(private val label: String) {
    /**
     * Represents the mobile vector.
     */
    MOBILE("APPPLAY"),

    /**
     * Represents the TV vector.
     */
    TV("TVPLAY");

    override fun toString(): String {
        return label
    }

    companion object {
        /**
         * Retrieves a [Vector] associated with the given [label].
         *
         * @param label The label to search for.
         * @return The [Vector] associated with the label, or `null` if not found.
         */
        fun fromLabel(label: String): Vector? {
            return entries.find { it.label.equals(label, ignoreCase = true) }
        }

        /**
         * Retrieves the vector based on the device type.
         *
         * @return The vector for the current device type.
         *
         * @receiver The [Context] used to access system resources.
         */
        fun Context.getVector(): Vector {
            val uiMode = resources.configuration.uiMode
            return if (uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION) {
                TV
            } else {
                MOBILE
            }
        }
    }
}
