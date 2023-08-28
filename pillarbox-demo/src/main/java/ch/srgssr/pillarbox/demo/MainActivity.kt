/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ch.srgssr.pillarbox.core.business.integrationlayer.service.KtorClient
import ch.srgssr.pillarbox.demo.data.DemoItem
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
                LaunchedEffect(Unit) {
                    Log.d("Coucou", KtorClient.getMediaComposition(DemoItem.OnDemandSquareVideo.uri).toString())
                }
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    MainNavigation()
                }
            }
        }
    }
}
