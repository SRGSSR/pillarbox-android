/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.srgssr.pillarbox.demo.R

/**
 * Home destinations
 *
 * @property route to navigate with NavController
 * @property labelResId string resource id
 * @property iconResId drawable resource id
 */
sealed class HomeDestination(val route: String, @StringRes val labelResId: Int, @DrawableRes val iconResId: Int) {
    /**
     * Streams home page
     */
    object Streams : HomeDestination("streams", R.string.streams, android.R.drawable.ic_menu_gallery)

    /**
     * Streams home page
     */
    object Playlists : HomeDestination("playlists", R.string.playlists, android.R.drawable.ic_menu_camera)

    /**
     * Streams home page
     */
    object Story : HomeDestination("story", R.string.story, android.R.drawable.ic_menu_compass)

    /**
     * Info home page
     */
    object Info : HomeDestination("info", R.string.info, android.R.drawable.ic_menu_info_details)
}
