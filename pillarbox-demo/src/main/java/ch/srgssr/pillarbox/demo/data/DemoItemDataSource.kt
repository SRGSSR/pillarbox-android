/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import android.content.Context
import com.google.gson.Gson

/**
 * Load DemoItem from assets folder.
 *
 * @property context
 * @constructor Create empty Demo item data source
 */
class DemoItemDataSource(private val context: Context) {

    /**
     * Load DemoItem list from assets folder.
     *
     * @param asset file name
     * @return list of DemoItem
     * @throws java.io.IOException
     */
    fun loadDemoItemFromAssets(asset: String): List<DemoItem> {
        return Gson().fromJson(context.loadJSONFromAssets(asset), Array<DemoItem>::class.java).asList()
    }

    private fun Context.loadJSONFromAssets(fileName: String): String {
        return applicationContext.assets.open(fileName).bufferedReader().use { reader ->
            reader.readText()
        }
    }
}
