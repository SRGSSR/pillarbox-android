/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Page event
 *
 * @property title The page event title.
 * @property levels The page event levels
 * @property fromPushNotification true to tell that page event from a push notification.
 * @property customLabels The page event custom labels.
 */
data class PageView(
    val title: String,
    val levels: Array<String> = emptyArray(),
    val fromPushNotification: Boolean = false,
    override val customLabels: CustomLabels? = null
) : BaseEvent {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PageView

        if (title != other.title) return false
        if (!levels.contentEquals(other.levels)) return false
        if (fromPushNotification != other.fromPushNotification) return false
        if (customLabels != other.customLabels) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + levels.contentHashCode()
        result = 31 * result + fromPushNotification.hashCode()
        result = 31 * result + (customLabels?.hashCode() ?: 0)
        return result
    }
}
