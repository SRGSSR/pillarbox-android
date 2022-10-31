/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import android.content.Context
import com.google.gson.Gson

/**
 * Load [Playlist] from assets folder.
 *
 * @property context
 */
class DemoPlaylistProvider(private val context: Context) {

    /**
     * Load DemoItem list from assets folder.
     *
     * @param asset file name
     * @return list of Playlist
     * @throws java.io.IOException
     */
    fun loadDemoItemFromAssets(asset: String): List<Playlist> {
        return Gson().fromJson(context.loadJSONFromAssets(asset), Array<Playlist>::class.java).asList()
    }

    private fun Context.loadJSONFromAssets(fileName: String): String {
        return applicationContext.assets.open(fileName).bufferedReader().use { reader ->
            reader.readText()
        }
    }
}
