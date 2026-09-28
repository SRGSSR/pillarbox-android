/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.Player
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import kotlinx.serialization.Serializable

/**
 * AspectRatioSwitchActivity
 *
 * @constructor Create empty ComponentActivity
 */
class AspectRatioSwitchActivity : ComponentActivity() {
    private val viewModel: AspectRatioSwitchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PillarboxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Route.Chooser,
                        modifier = Modifier.safeDrawingPadding(),
                    ) {
                        composable<Route.Chooser> {
                            MediaChooser(
                                items = Items,
                                onItemClick = { item ->
                                    viewModel.play(item)
                                    navController.navigate(Route.Player)
                                },
                            )
                        }
                        composable<Route.Player> {
                            SimplePlayer(player = viewModel.player)
                        }
                    }
                }
            }
        }
    }

    private sealed interface Route {
        @Serializable
        data object Chooser : Route

        @Serializable
        data object Player : Route
    }

    private companion object {
        private val Items = listOf(
            SamplesSRG.OnDemandVerticalVideo,
            SamplesSRG.OnDemandHorizontalVideo,
            SamplesSRG.OnDemandSquareVideo,
        )
    }
}

@Composable
private fun MediaChooser(
    items: List<DemoItem>,
    onItemClick: (DemoItem) -> Unit,
) {
    Column(modifier = Modifier.padding(MaterialTheme.paddings.baseline)) {
        DemoListSectionView {
            items.forEachIndexed { index, item ->
                DemoListItemView(
                    title = item.title.orEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = item.description,
                    languageTag = item.languageTag,
                    onClick = { onItemClick(item) },
                )

                if (index < items.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SimplePlayer(player: Player) {
    val presentationState = rememberPresentationState(player)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
    ) {
        PlayerSurface(
            player = player,
            modifier = Modifier
                .align(Alignment.Center)
                .clipToBounds()
                .resizeWithContentScale(
                    contentScale = ContentScale.Fit,
                    sourceSizeDp = presentationState.videoSizeDp,
                ),
            surfaceType = SURFACE_TYPE_SURFACE_VIEW,
        )
    }
}
