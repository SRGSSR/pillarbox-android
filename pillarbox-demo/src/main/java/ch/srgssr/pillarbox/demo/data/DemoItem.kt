/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

/**
 * Item type
 *
 * @constructor Create empty Item type
 */
enum class ItemType {
    MEDIA,
    CUSTOM
}

/**
 * Demo item
 *
 * @property type
 * @property id
 * @property title
 * @property description
 * @property uri
 * @constructor Create empty Demo item
 */
data class DemoItem(val type: ItemType, val id: String, val title: String, val description: String? = null, val uri: String? = null)
