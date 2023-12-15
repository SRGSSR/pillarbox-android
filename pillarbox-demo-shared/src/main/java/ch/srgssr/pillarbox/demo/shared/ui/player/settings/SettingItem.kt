/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represent a setting item.
 */
data class SettingItem(
    /**
     * The title of the setting.
     */
    val title: String,

    /**
     * The optional subtitle of the setting.
     */
    val subtitle: String?,

    /**
     * The icon of the setting.
     */
    val icon: ImageVector,

    /**
     * The route of the setting.
     */
    val destination: SettingsRoutes
)
