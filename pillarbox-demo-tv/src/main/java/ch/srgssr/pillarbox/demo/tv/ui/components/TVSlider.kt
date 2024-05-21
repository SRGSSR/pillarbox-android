/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import ch.srgssr.pillarbox.demo.tv.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme

/**
 * Slider component suited for use on TV.
 *
 * @param value The current value of this slider.
 * @param range The range of values supported by this slider.
 * @param compactMode If `true`, the slider will be thinner.
 * @param modifier The [Modifier] to apply to the layout.
 * @param enabled Whether or not this slider is enabled.
 * @param onSeekBack The action to perform when seeking back.
 * @param onSeekForward The action to perform when seeking forward.
 */
@Composable
fun TVSlider(
    value: Long,
    range: LongRange,
    compactMode: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
) {
    val seekBarHeight by animateDpAsState(targetValue = if (compactMode) 8.dp else 16.dp, label = "seek_bar_height")
    val thumbColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        label = "thumb_color",
    )

    val activeTrackWeight by animateFloatAsState(targetValue = value / range.last.toFloat(), label = "active_track_weight")
    val activeTrackColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        label = "active_track_color",
    )

    val inactiveTrackWeight by animateFloatAsState(targetValue = 1f - activeTrackWeight, label = "inactive_track_weight")
    val inactiveTrackColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        label = "inactive_track_color",
    )

    Row(
        modifier = modifier.height(seekBarHeight),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Track(
            weight = activeTrackWeight,
            color = activeTrackColor,
        )

        Thumb(
            color = thumbColor,
            enabled = enabled,
            onSeekBack = onSeekBack,
            onSeekForward = onSeekForward,
        )

        Track(
            weight = inactiveTrackWeight,
            color = inactiveTrackColor,
        )
    }
}

@Composable
private fun RowScope.Track(
    weight: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (weight > 0f) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .weight(weight)
                .background(
                    color = color,
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun Thumb(
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
            .background(
                color = color,
                shape = CircleShape,
            )
            .then(
                if (enabled) {
                    Modifier
                        .focusable()
                        .onDpadEvent(
                            onLeft = {
                                onSeekBack()
                                true
                            },
                            onRight = {
                                onSeekForward()
                                true
                            },
                        )
                } else {
                    Modifier
                }
            ),
    )
}

@Composable
@PreviewLightDark
private fun TVSliderPreview(
    @PreviewParameter(TVSliderPreviewParameters::class) previewParameters: PreviewParameters,
) {
    var progress by remember {
        mutableLongStateOf(previewParameters.initialValue)
    }

    PillarboxTheme {
        TVSlider(
            value = progress,
            range = 0L..100L,
            compactMode = previewParameters.compactMode,
            enabled = previewParameters.enabled,
            onSeekBack = { progress-- },
            onSeekForward = { progress++ },
        )
    }
}

private class PreviewParameters(
    val compactMode: Boolean,
    val enabled: Boolean,
    val initialValue: Long,
)

private class TVSliderPreviewParameters : PreviewParameterProvider<PreviewParameters> {
    override val values = sequence {
        listOf(false, true).forEach { compactMode ->
            listOf(false, true).forEach { enabled ->
                listOf(0L, 50L, 100L).forEach { initialValue ->
                    yield(
                        PreviewParameters(
                            compactMode = compactMode,
                            enabled = enabled,
                            initialValue = initialValue,
                        )
                    )
                }
            }
        }
    }
}
