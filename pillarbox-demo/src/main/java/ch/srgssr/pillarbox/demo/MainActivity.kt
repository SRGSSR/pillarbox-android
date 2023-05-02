/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import ch.srgssr.pillarbox.analytics.PageView
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.ui.MainNavigation
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.trackPageView()
        setContent {
            PillarboxTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.surface) {
                    MainNavigation()
                }
            }
        }
    }
}

/**
 * Main view model to store SRGAnalytics
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Track page view
     */
    fun trackPageView() {
        SRGAnalytics.sendPageView(PageView("main", levels = arrayOf("app", "pillarbox")))
    }
}
