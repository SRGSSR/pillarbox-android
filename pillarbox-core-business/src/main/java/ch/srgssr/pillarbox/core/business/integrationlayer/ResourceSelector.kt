/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import ch.srg.dataProvider.integrationlayer.data.remote.Chapter
import ch.srg.dataProvider.integrationlayer.data.remote.Resource
import ch.srg.dataProvider.integrationlayer.data.remote.Resource.Drm
import ch.srg.dataProvider.integrationlayer.data.remote.StreamingMethod.DASH
import ch.srg.dataProvider.integrationlayer.data.remote.StreamingMethod.HLS
import ch.srg.dataProvider.integrationlayer.data.remote.StreamingMethod.PROGRESSIVE

/**
 * Select a [Resource] from [Chapter.resourceList]
 */
internal class ResourceSelector {
    /**
     * Select the first resource from chapter that is playable by the Player.
     *
     * @param chapter
     * @return null if no compatible resource is found.
     */
    fun selectResourceFromChapter(chapter: Chapter): Resource? {
        return chapter.resourceList?.firstOrNull {
            val streamingMethod = it.streamingMethod
            val drmList = it.drmList

            (streamingMethod == DASH || streamingMethod == HLS || streamingMethod == PROGRESSIVE) &&
                (drmList == null || drmList.any { drm -> drm.type == Drm.Type.WIDEVINE })
        }
    }
}
