/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct

/**
 * Custom labels
 *
 * @property commandersActLabels
 * @property comScoreLabels
 * @constructor Create empty Custom labels
 */
class CustomLabels internal constructor(
    val commandersActLabels: Map<String, String>?,
    val comScoreLabels: Map<String, String>?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomLabels

        if (commandersActLabels != other.commandersActLabels) return false
        if (comScoreLabels != other.comScoreLabels) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commandersActLabels?.hashCode() ?: 0
        result = 31 * result + (comScoreLabels?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CustomLabels(commandersActLabels=$commandersActLabels, comScoreLabels=$comScoreLabels)"
    }

    /**
     * Builder to help building CustomLabels
     *
     * @property commandersActLabels initial CommandersAct custom labels
     * @property comScoreLabels initial ComScore custom labels
     */
    class Builder(
        private var commandersActLabels: MutableMap<String, String>? = null,
        private var comScoreLabels: MutableMap<String, String>? = null
    ) {
        private var userId: String? = null

        /**
         * Set user id inside [commandersActLabels] with key [CommandersAct.KEY_USER_ID]
         * Will set [CommandersAct.KEY_USER_IS_LOGGED] to true or false depending of [userId] nullity.
         *
         * @param userId userId value to set
         */
        fun setUserId(userId: String?): Builder {
            this.userId = userId
            return this
        }

        /**
         * Put custom label to both [commandersActLabels] and [comScoreLabels]
         *
         * @param key key of the custom label
         * @param value value of the custom label
         */
        fun putBothLabel(key: String, value: String): Builder {
            putCommandersActLabel(key, value)
            return putComScoreLabel(key, value)
        }

        /**
         * Add CommandersAct to custom labels
         *
         * @param key key of the custom label
         * @param value value of the custom label
         */
        fun putCommandersActLabel(key: String, value: String): Builder {
            if (commandersActLabels == null) {
                commandersActLabels = HashMap()
            }
            commandersActLabels?.let {
                it[key] = value
            }
            return this
        }

        /**
         * Add ComScore to custom labels
         *
         * @param key key of the custom label
         * @param value value of the custom label
         */
        fun putComScoreLabel(key: String, value: String): Builder {
            if (comScoreLabels == null) {
                comScoreLabels = HashMap()
            }
            comScoreLabels?.let {
                it[key] = value
            }
            return this
        }

        /**
         * Set and replace CommandersAct custom labels
         *
         * @param commandersActLabels custom labels to set
         */
        fun setCommandersActLabels(commandersActLabels: Map<String, String>?): Builder {
            this.commandersActLabels = commandersActLabels?.toMutableMap()
            return this
        }

        /**
         * Set and replace ComScore custom labels
         *
         * @param comScoreLabels custom labels to set
         */
        fun setComScoreLabels(comScoreLabels: Map<String, String>?): Builder {
            this.comScoreLabels = comScoreLabels?.toMutableMap()
            return this
        }

        /**
         * Build a new CustomLabels
         */
        fun build(): CustomLabels {
            val isLogged: Boolean = userId?.let {
                putCommandersActLabel(CommandersAct.KEY_USER_ID, it)
                true
            } ?: false
            putCommandersActLabel(CommandersAct.KEY_USER_IS_LOGGED, isLogged.toString())
            return CustomLabels(commandersActLabels = commandersActLabels, comScoreLabels = comScoreLabels)
        }
    }
}
