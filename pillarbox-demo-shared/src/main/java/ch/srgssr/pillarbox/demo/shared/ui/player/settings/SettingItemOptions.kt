/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

/**
 * The options for a specific setting item.
 *
 * @param T The type of option.
 * @property title The title of the setting.
 * @property items The list of possible items.
 * @property disabled `true` if this kind of tracks is disabled, `false` otherwise.
 */
data class SettingItemOptions<T>(
    val title: String,
    val items: List<T>,
    val disabled: Boolean,
)
