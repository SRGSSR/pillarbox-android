/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.playlists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Showcase allowing the user to change the repeat mode and decide if the current media item should pause when it ends.
 *
 * @param playlist The [Playlist] to play.
 * @param modifier The [Modifier] to apply to the layout.
 */
@Composable
fun CustomPlaybackSettingsShowcase(
    playlist: Playlist,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember(playlist) {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItems(playlist.items.map { it.toMediaItem() })
            prepare()
            play()
        }
    }

    val repeatModes = listOf(
        Player.REPEAT_MODE_OFF to stringResource(R.string.repeat_mode_off),
        Player.REPEAT_MODE_ONE to stringResource(R.string.repeat_mode_one),
        Player.REPEAT_MODE_ALL to stringResource(R.string.repeat_mode_all),
    )

    var pauseAtEndOfItem by remember { mutableStateOf(player.pauseAtEndOfMediaItems) }

    Column(modifier = modifier) {
        Box {
            var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
            var showRepeatModeMenu by remember { mutableStateOf(false) }
            var selectedRepeatModeIndex by remember {
                mutableIntStateOf(
                    repeatModes.indexOfFirst { (repeatMode, _) ->
                        repeatMode == player.repeatMode
                    }
                )
            }

            val interactionSource = remember { MutableInteractionSource() }

            Row(
                modifier = Modifier
                    .semantics(mergeDescendants = true) {}
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val pressInteraction = PressInteraction.Press(it)

                                interactionSource.emit(pressInteraction)

                                menuOffset = DpOffset(
                                    x = it.x.toDp(),
                                    y = (it.y - size.height).toDp(),
                                )
                                showRepeatModeMenu = true

                                if (tryAwaitRelease()) {
                                    interactionSource.emit(PressInteraction.Release(pressInteraction))
                                } else {
                                    interactionSource.emit(PressInteraction.Cancel(pressInteraction))
                                }
                            }
                        )
                    }
                    .indication(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                    )
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = MaterialTheme.paddings.baseline),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.repeat_mode))

                Text(text = repeatModes[selectedRepeatModeIndex].second)
            }

            DropdownMenu(
                expanded = showRepeatModeMenu,
                onDismissRequest = { showRepeatModeMenu = false },
                modifier = Modifier.semantics {
                    collectionInfo = CollectionInfo(rowCount = repeatModes.size, columnCount = 1)
                },
                offset = menuOffset,
            ) {
                repeatModes.forEachIndexed { index, (repeatMode, repeatModeLabel) ->
                    val isSelected = index == selectedRepeatModeIndex

                    DropdownMenuItem(
                        text = { Text(text = repeatModeLabel) },
                        onClick = {
                            selectedRepeatModeIndex = index
                            player.repeatMode = repeatMode
                            showRepeatModeMenu = false
                        },
                        modifier = Modifier.semantics {
                            selected = isSelected
                            collectionItemInfo = CollectionItemInfo(
                                rowIndex = index,
                                rowSpan = 1,
                                columnIndex = 1,
                                columnSpan = 1,
                            )
                        },
                        leadingIcon = {
                            AnimatedVisibility(isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                )
                            }
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(pauseAtEndOfItem) {
                    pauseAtEndOfItem = !pauseAtEndOfItem
                    player.pauseAtEndOfMediaItems = pauseAtEndOfItem
                }
                .padding(
                    horizontal = MaterialTheme.paddings.baseline,
                    vertical = MaterialTheme.paddings.small,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.pause_end_media_items))

            Switch(
                checked = pauseAtEndOfItem,
                onCheckedChange = null,
            )
        }

        DemoPlayerView(
            player = player,
            displayPlaylist = true,
        )

        LifecycleResumeEffect(player) {
            player.play()
            onPauseOrDispose {
                player.pause()
            }
        }
        DisposableEffect(player) {
            onDispose {
                player.release()
            }
        }
    }
}
