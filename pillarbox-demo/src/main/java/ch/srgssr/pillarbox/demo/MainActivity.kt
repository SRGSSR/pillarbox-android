/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import ch.srgssr.pillarbox.analytics.PageEvent
import ch.srgssr.pillarbox.demo.ui.MainNavigation
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PillarboxTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        trackPageView(PageEvent("main", levels = arrayOf("app", "pillarbox")))
    }
}
