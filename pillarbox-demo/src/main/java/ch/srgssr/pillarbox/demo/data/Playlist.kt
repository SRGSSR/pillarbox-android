/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import java.io.Serializable

/**
 * Playlist
 *
 * @property title
 * @property items
 * @property description optional
 */
data class Playlist(val title: String, val items: List<DemoItem>, val description: String? = null) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
