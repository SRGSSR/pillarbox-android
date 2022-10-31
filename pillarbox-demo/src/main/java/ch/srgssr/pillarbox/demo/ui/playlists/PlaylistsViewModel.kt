/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.playlists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ch.srgssr.pillarbox.demo.data.DemoPlaylistProvider
import ch.srgssr.pillarbox.demo.data.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Demo list view model
 *
 * @param application
 */
class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * List demo item
     */
    val listPlaylist: StateFlow<List<Playlist>> = MutableStateFlow(DemoPlaylistProvider(application).loadDemoItemFromAssets("playlists.json"))
}
