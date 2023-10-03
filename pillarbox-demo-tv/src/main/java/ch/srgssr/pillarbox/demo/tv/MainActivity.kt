/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:OptIn(ExperimentalTvMaterial3Api::class)

package ch.srgssr.pillarbox.demo.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.tv.examples.ExamplesHome
import ch.srgssr.pillarbox.demo.tv.player.PlayerActivity

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    shape = RectangleShape,
                    border = Border.None
                ) {
                    MainView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = HorizontalPadding,
                                end = HorizontalPadding,
                                top = VerticalPadding,
                            ),
                        this@MainActivity::openPlayer
                    )
                }
            }
        }
    }

    private fun openPlayer(item: DemoItem) {
        PlayerActivity.startPlayer(this, item)
    }

    @Composable
    private fun MainView(
        modifier: Modifier = Modifier,
        onItemSelected: (DemoItem) -> Unit
    ) {
        ExamplesHome(modifier = modifier, onItemSelected = onItemSelected)
    }

    companion object {
        private val HorizontalPadding = 32.dp
        private val VerticalPadding = 16.dp
    }
}
