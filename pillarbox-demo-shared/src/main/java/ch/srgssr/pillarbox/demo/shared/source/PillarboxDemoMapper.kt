/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import ch.srgssr.pillarbox.standard.PlayerData
import ch.srgssr.pillarbox.standard.PlayerDataMapper

class PillarboxDemoMapper : PlayerDataMapper<CustomData> by PlayerDataMapper.Default() {

    override fun PlayerData<CustomData>.mediaItemTrackerData(mutableMediaItemTrackerData: MutableMediaItemTrackerData) {
        customData?.let {
            // Configure your damn trackers here!
            // mutableMediaItemTrackerData[ComScoreTracker::class.java] = FactoryData(factory = ComScoreTracker.Factory(),data = ComScoreTracker.Data(emptyMap()))
        }
    }
}
