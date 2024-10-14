/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource

/**
 * Select a [Resource] from [Chapter.listResource]
 */
internal class ResourceSelector {
    /**
     * Select the first resource from [chapter] that is playable by the Player.
     *
     * @param chapter The [Chapter].
     * @return null if no compatible resource is found.
     */
    @Suppress("SwallowedException")
    fun selectResourceFromChapter(chapter: Chapter): Resource? {
        return chapter.listResource?.find {
            (it.type == Resource.Type.DASH || it.type == Resource.Type.HLS || it.type == Resource.Type.PROGRESSIVE) &&
                (it.drmList.isNullOrEmpty() || it.drmList.any { drm -> drm.type == Drm.Type.WIDEVINE })
        }
    }
}
