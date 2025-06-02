/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            PillarboxTheme {
                MainNavigation()
            }
        }
    }
}
