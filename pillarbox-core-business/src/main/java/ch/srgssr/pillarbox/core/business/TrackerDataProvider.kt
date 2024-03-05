/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * Tracker data provider to add some data for custom tracker.
 */
fun interface TrackerDataProvider {
    /**
     * Update tracker data with given integration layer data.
     *
     * @param trackerData The [MediaItemTrackerData.Builder] to update.
     * @param resource The selected [Resource].
     * @param chapter The selected [Chapter].
     * @param mediaComposition The loaded [MediaComposition].
     */
    fun update(
        trackerData: MediaItemTrackerData.Builder,
        resource: Resource,
        chapter: Chapter,
        mediaComposition: MediaComposition
    )
}
