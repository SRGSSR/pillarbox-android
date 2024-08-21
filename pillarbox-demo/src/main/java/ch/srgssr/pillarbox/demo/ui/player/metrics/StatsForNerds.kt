/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.metrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
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
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import kotlin.random.Random

@Composable
internal fun StatsForNerds(
    playbackMetrics: PlaybackMetrics,
    modifier: Modifier = Modifier,
) {
    val statsForNerdsViewModel = viewModel<StatsForNerdsViewModel>(key = playbackMetrics.sessionId)
    val startupTimes by statsForNerdsViewModel.startupTimes.collectAsState()
    val information by statsForNerdsViewModel.information.collectAsState()
    val indicatedBitRates by statsForNerdsViewModel.indicatedBitRates.collectAsState()
    val observedBitRates by statsForNerdsViewModel.observedBitRates.collectAsState()
    val volumes by statsForNerdsViewModel.volumes.collectAsState()
    val stalls by statsForNerdsViewModel.stalls.collectAsState()

    LaunchedEffect(playbackMetrics) {
        statsForNerdsViewModel.playbackMetrics = playbackMetrics
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(modifier),
    ) {
        Text(
            text = stringResource(R.string.stats_for_nerds),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge,
        )

        GenericSection(
            title = stringResource(R.string.startup_times),
            entries = startupTimes,
        )

        GenericSection(
            title = stringResource(R.string.media_information),
            entries = information,
        )

        IndicatedBitrate(indicatedBitRates)

        ObservedBitrate(observedBitRates)

        DataVolume(volumes)

        Stalls(stalls)
    }
}

@Composable
private fun GenericSection(
    title: String,
    entries: Map<String, String>,
) {
    if (entries.isEmpty()) {
        return
    }

    Section(title) {
        var index = 0
        entries.forEach { (label, value) ->
            DemoListItemView(
                leadingText = label,
                trailingText = value,
                modifier = Modifier.fillMaxWidth(),
            )

            if (index < entries.size - 1) {
                HorizontalDivider()
            }

            index++
        }
    }
}

@Composable
private fun IndicatedBitrate(
    bitRates: BitRates,
) {
    if (bitRates.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.indicated_bitrate),
        unit = bitRates.unit,
        bitRates = bitRates,
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
private fun ObservedBitrate(
    bitRates: BitRates,
) {
    if (bitRates.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.observed_bitrate),
        unit = bitRates.unit,
        bitRates = bitRates,
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
private fun DataVolume(
    volumes: DataVolumes,
) {
    if (volumes.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.data_volume),
        unit = volumes.unit,
        legend = stringResource(R.string.total_volume, volumes.total),
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
private fun Stalls(
    stalls: Stalls,
) {
    if (stalls.data.isEmpty()) {
        return
    }

    Chart(
        title = stringResource(R.string.stalls),
        legend = stringResource(R.string.total_stalls, stalls.total),
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
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    DemoListHeaderView(
        title = title,
        modifier = Modifier.padding(start = MaterialTheme.paddings.baseline),
    )

    DemoListSectionView {
        content()
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun Chart(
    title: String,
    unit: String,
    bitRates: BitRates,
    content: @Composable ColumnScope.() -> Unit,
) {
    Section(title = title) {
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
                    .padding(
                        horizontal = MaterialTheme.paddings.baseline,
                        vertical = MaterialTheme.paddings.small,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = stringResource(R.string.minimum_value, bitRates.min))

                Text(text = stringResource(R.string.current_value, bitRates.current))

                Text(text = stringResource(R.string.maximum_value, bitRates.max))
            }
        }
    }
}

@Composable
private fun Chart(
    title: String,
    unit: String? = null,
    legend: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Section(title = title) {
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
    }
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
                )
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
            )
        }
    }
}
