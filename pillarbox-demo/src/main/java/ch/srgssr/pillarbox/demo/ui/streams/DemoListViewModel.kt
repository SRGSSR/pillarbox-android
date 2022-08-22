/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.streams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.DemoItemDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Demo list view model
 *
 * @param application
 */
class DemoListViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * List demo item
     */
    val listDemoItem: StateFlow<List<DemoItem>> = MutableStateFlow(DemoItemDataSource(application).loadDemoItemFromAssets("streams.json"))
}
