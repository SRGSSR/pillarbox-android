/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.ui.extension.handleDPadKeyEvents
import kotlinx.coroutines.launch

/**
 * Toggle view
 *
 * @param visibilityState A state that hold the current visibility and auto hide delay mode.
 * @param toggleableContent Content to appear or disappear based on the value of [ToggleVisibilityState.isDisplayed]
 * @param content Content displayed under toggleableContent
 * @param modifier modifier for the Layout created to contain the content
 * @param contentAlignment - The default alignment inside the Box.
 * @param propagateMinConstraints - Whether the incoming min constraints should be passed to content.
 * @param enter EnterTransition(s) used for the appearing animation, fading in while expanding by default
 * @param exit ExitTransition(s) used for the disappearing animation, fading out while shrinking by default
 * @param toggleLabel Accessibility label for clickable content
 */
@Composable
fun ToggleView(
    visibilityState: ToggleVisibilityState,
    toggleableContent: @Composable AnimatedVisibilityScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    enter: EnterTransition = expandVertically { it },
    exit: ExitTransition = shrinkVertically { it },
    toggleLabel: String? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    role = Role.Switch,
                    onClickLabel = toggleLabel,
                    onClick = {
                        coroutineScope.launch {
                            visibilityState.toggle()
                        }
                    }
                )
                .handleDPadKeyEvents(
                    onEnter = {
                        coroutineScope.launch {
                            visibilityState.toggle()
                        }
                    }
                )
                .focusable()
        ) {
            content()
        }
        AnimatedVisibility(
            modifier = Modifier
                .matchParentSize()
                .onFocusChanged {
                    if (it.hasFocus) {
                        coroutineScope.launch {
                            visibilityState.show()
                        }
                    }
                },
            visible = visibilityState.isVisible,
            enter = enter,
            exit = exit,
            content = toggleableContent,
        )
    }
}

@Preview
@Composable
private fun TogglePreview() {
    var delay: AutoHideMode by remember {
        mutableStateOf(AutoHideMode.Delayed(2))
    }
    val visibilityState = rememberAutoHideState(delay)
    val coroutineScope = rememberCoroutineScope()
    Column {
        ToggleView(
            modifier = Modifier.aspectRatio(16 / 9f),
            visibilityState = visibilityState,
            toggleableContent = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BasicText(text = "Text to hide", color = { Color.Red })
                }
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = "Show",
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        visibilityState.show()
                    }
                }
            )
            BasicText(
                text = "Toggle",
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        visibilityState.toggle()
                    }
                }
            )
            BasicText(
                text = "Hide",
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        visibilityState.hide()
                    }
                }
            )
            BasicText(
                text = "Disable",
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        delay = AutoHideMode.Disable
                    }
                }
            )
            BasicText(
                text = "Enable",
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        delay = AutoHideMode.Delayed(2)
                    }
                }
            )
        }
    }
}
