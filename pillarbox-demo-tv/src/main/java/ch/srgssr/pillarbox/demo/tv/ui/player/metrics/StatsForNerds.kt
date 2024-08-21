/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.metrics

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.ListItemShape
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.components.BarChart
import ch.srgssr.pillarbox.demo.shared.ui.components.LineChart
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.BitRates
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.DataVolumes
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.Stalls
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.StatsForNerdsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.StatsForNerdsViewModel.Companion.CHART_ASPECT_RATIO
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.StatsForNerdsViewModel.Companion.CHART_MAX_POINTS
import ch.srgssr.pillarbox.demo.shared.ui.theme.ColorChartDataVolume
import ch.srgssr.pillarbox.demo.shared.ui.theme.ColorChartIndicatedBitrate
import ch.srgssr.pillarbox.demo.shared.ui.theme.ColorChartObservedBitrate
import ch.srgssr.pillarbox.demo.shared.ui.theme.ColorChartStalls
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.qos.models.QoETimings
import ch.srgssr.pillarbox.player.qos.models.QoSTimings
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.random.Random

@Composable
internal fun StatsForNerds(
    qoeTimings: QoETimings?,
    qosTimings: QoSTimings?,
    playbackMetrics: PlaybackMetrics,
    modifier: Modifier = Modifier,
) {
    val statsForNerdsViewModel = viewModel<StatsForNerdsViewModel>(key = playbackMetrics.sessionId)
    val qoeTimingsFields by statsForNerdsViewModel.qosTimingsFields.collectAsState()
    val qosTimingsFields by statsForNerdsViewModel.qosTimingsFields.collectAsState()
    val information by statsForNerdsViewModel.information.collectAsState()
    val indicatedBitRates by statsForNerdsViewModel.indicatedBitRates.collectAsState()
    val observedBitRates by statsForNerdsViewModel.observedBitRates.collectAsState()
    val volumes by statsForNerdsViewModel.volumes.collectAsState()
    val stalls by statsForNerdsViewModel.stalls.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val onFocusAcquired: (Float) -> Unit = { itemTop: Float ->
        coroutineScope.launch {
            val scrollValue = (itemTop - scrollState.viewportSize / 2f)
                .toInt()
                .coerceIn(0, scrollState.maxValue)

            scrollState.animateScrollTo(scrollValue)
        }
    }

    LaunchedEffect(qoeTimings) {
        statsForNerdsViewModel.qoeTimings = qoeTimings
    }

    LaunchedEffect(qosTimings) {
        statsForNerdsViewModel.qosTimings = qosTimings
    }

    LaunchedEffect(playbackMetrics) {
        statsForNerdsViewModel.playbackMetrics = playbackMetrics
    }

    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(top = MaterialTheme.paddings.baseline)
            .verticalScroll(scrollState),
    ) {
        Text(
            text = stringResource(R.string.stats_for_nerds),
            style = MaterialTheme.typography.titleMedium,
        )

        GenericSection(
            title = stringResource(R.string.qoe_timings),
            entries = qoeTimingsFields,
            onFocusAcquired = onFocusAcquired,
        )

        GenericSection(
            title = stringResource(R.string.qos_timings),
            entries = qosTimingsFields,
            onFocusAcquired = onFocusAcquired,
        )

        GenericSection(
            title = stringResource(R.string.media_information),
            entries = information,
            onFocusAcquired = onFocusAcquired,
        )

        IndicatedBitrate(
            bitRates = indicatedBitRates,
            onFocusAcquired = onFocusAcquired,
        )

        ObservedBitrate(
            bitRates = observedBitRates,
            onFocusAcquired = onFocusAcquired,
        )

        DataVolume(
            volumes = volumes,
            onFocusAcquired = onFocusAcquired,
        )

        Stalls(
            stalls = stalls,
            onFocusAcquired = onFocusAcquired,
        )

        Spacer(Modifier.height(MaterialTheme.paddings.baseline))
    }
}

@Composable
private fun ColumnScope.GenericSection(
    title: String,
    entries: Map<String, String>,
    onFocusAcquired: (itemTop: Float) -> Unit,
) {
    if (entries.isEmpty()) {
        return
    }

    SectionTitle(title)

    entries.forEach { (label, value) ->
        SelectableEntry(
            content = { Text(text = label) },
            supportingContent = { Text(text = value) },
            shape = ListItemDefaults.shape(RoundedCornerShape(50)),
            onFocusAcquired = onFocusAcquired,
        )
    }
}

@Composable
private fun ColumnScope.IndicatedBitrate(
    bitRates: BitRates,
    onFocusAcquired: (itemTop: Float) -> Unit,
) {
    if (bitRates.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.indicated_bitrate),
        unit = bitRates.unit,
        bitRates = bitRates,
        onFocusAcquired = onFocusAcquired,
        content = {
            LineChart(
                data = bitRates.data,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(CHART_ASPECT_RATIO),
                lineColor = ColorChartIndicatedBitrate,
                stretchChartToPointsCount = CHART_MAX_POINTS,
                scaleTextStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                ),
            )
        },
    )
}

@Composable
private fun ColumnScope.ObservedBitrate(
    bitRates: BitRates,
    onFocusAcquired: (itemTop: Float) -> Unit,
) {
    if (bitRates.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.observed_bitrate),
        unit = bitRates.unit,
        bitRates = bitRates,
        onFocusAcquired = onFocusAcquired,
        content = {
            LineChart(
                data = bitRates.data,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(CHART_ASPECT_RATIO),
                lineColor = ColorChartObservedBitrate,
                stretchChartToPointsCount = CHART_MAX_POINTS,
                scaleTextStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                ),
            )
        },
    )
}

@Composable
private fun ColumnScope.DataVolume(
    volumes: DataVolumes,
    onFocusAcquired: (itemTop: Float) -> Unit,
) {
    if (volumes.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.data_volume),
        unit = volumes.unit,
        legend = stringResource(R.string.total_volume, volumes.total),
        onFocusAcquired = onFocusAcquired,
        content = {
            BarChart(
                data = volumes.data,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(CHART_ASPECT_RATIO),
                barColor = ColorChartDataVolume,
                stretchChartToPointsCount = CHART_MAX_POINTS,
                scaleTextStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                ),
            )
        },
    )
}

@Composable
private fun ColumnScope.Stalls(
    stalls: Stalls,
    onFocusAcquired: (itemTop: Float) -> Unit,
) {
    if (stalls.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.stalls),
        legend = stringResource(R.string.total_stalls, stalls.total),
        onFocusAcquired = onFocusAcquired,
        content = {
            LineChart(
                data = stalls.data,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(CHART_ASPECT_RATIO),
                lineColor = ColorChartStalls,
                stretchChartToPointsCount = CHART_MAX_POINTS,
                scaleTextStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                ),
            )
        },
    )
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(vertical = MaterialTheme.paddings.baseline),
        style = MaterialTheme.typography.titleSmall,
    )
}

@Composable
private fun SelectableEntry(
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    shape: ListItemShape = ListItemDefaults.shape(),
    onFocusAcquired: (itemTop: Float) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var itemTop by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            onFocusAcquired(itemTop)
        }
    }

    ListItem(
        selected = false,
        onClick = {},
        headlineContent = { content?.invoke() },
        modifier = modifier.onGloballyPositioned { layoutCoordinates ->
            val y = layoutCoordinates.positionInParent().y
            val height = layoutCoordinates.size.height

            itemTop = y + (height / 2f)
        },
        supportingContent = supportingContent,
        shape = shape,
        interactionSource = interactionSource,
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ColumnScope.Chart(
    title: String,
    unit: String,
    bitRates: BitRates,
    onFocusAcquired: (itemTop: Float) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    SectionTitle(title = title)

    SelectableEntry(
        content = {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                Text(
                    text = unit,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(vertical = MaterialTheme.paddings.small)
                        .padding(end = MaterialTheme.paddings.small),
                )

                content()

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.paddings.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = stringResource(R.string.minimum_value, bitRates.min))

                    Text(text = stringResource(R.string.current_value, bitRates.current))

                    Text(text = stringResource(R.string.maximum_value, bitRates.max))
                }
            }
        },
        onFocusAcquired = onFocusAcquired,
    )
}

@Composable
private fun ColumnScope.Chart(
    title: String,
    unit: String? = null,
    legend: String,
    onFocusAcquired: (itemTop: Float) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    SectionTitle(title = title)

    SelectableEntry(
        content = {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                if (unit != null) {
                    Text(
                        text = unit,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(vertical = MaterialTheme.paddings.small)
                            .padding(end = MaterialTheme.paddings.small),
                    )
                }

                content()

                Text(
                    text = legend,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(
                            horizontal = MaterialTheme.paddings.baseline,
                            vertical = MaterialTheme.paddings.small,
                        ),
                )
            }
        },
        onFocusAcquired = onFocusAcquired,
    )
}

@Composable
@Preview(showBackground = true)
private fun GenericSectionPreview() {
    PillarboxTheme {
        Column {
            GenericSection(
                title = "Section title",
                entries = (0 until 5).associate { index ->
                    "Label ${index + 1}" to "Value ${index + 1}"
                },
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun GenericSectionEmptyPreview() {
    PillarboxTheme {
        Column {
            GenericSection(
                title = "Section title",
                entries = emptyMap(),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun IndicatedBitratePreview() {
    val dataSize = 10

    PillarboxTheme {
        Column {
            IndicatedBitrate(
                bitRates = BitRates(
                    data = buildList(dataSize) {
                        repeat(dataSize) {
                            add(6f)
                        }
                    },
                ),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun IndicatedBitrateEmptyPreview() {
    PillarboxTheme {
        Column {
            IndicatedBitrate(
                bitRates = BitRates(data = emptyList()),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ObservedBitratePreview() {
    val dataSize = 10
    val minValue = 60f
    val maxValue = 120f

    PillarboxTheme {
        Column {
            ObservedBitrate(
                bitRates = BitRates(
                    data = buildList(dataSize) {
                        repeat(dataSize) {
                            add(minValue + Random.nextFloat() * (maxValue - minValue))
                        }
                    },
                ),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ObservedBitrateEmptyPreview() {
    PillarboxTheme {
        Column {
            ObservedBitrate(
                bitRates = BitRates(data = emptyList()),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun DataVolumePreview() {
    val dataSize = 10
    val minValue = 0f
    val maxValue = 50f
    val data = buildList(dataSize) {
        repeat(dataSize) {
            add(minValue + Random.nextFloat() * (maxValue - minValue))
        }
    }

    PillarboxTheme {
        Column {
            DataVolume(
                volumes = DataVolumes(
                    data = data,
                    total = data.sum().toString(),
                ),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun DataVolumeEmptyPreview() {
    PillarboxTheme {
        Column {
            DataVolume(
                volumes = DataVolumes(emptyList(), ""),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun StallsPreview() {
    val dataSize = 10
    val minValue = 0
    val maxValue = 5
    val data = buildList(dataSize) {
        repeat(dataSize) {
            add(Random.nextInt(minValue, maxValue).toFloat())
        }
    }

    PillarboxTheme {
        Column {
            Stalls(
                stalls = Stalls(
                    data = data,
                    total = data.sum().toString(),
                ),
                onFocusAcquired = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun StallsEmptyPreview() {
    PillarboxTheme {
        Column {
            Stalls(
                stalls = Stalls(emptyList(), ""),
                onFocusAcquired = {},
            )
        }
    }
}
