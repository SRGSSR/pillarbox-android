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
     * Select the first resource from chapter that is playable by the Player.
     *
     * @param chapter
     * @return null if no compatible resource is found.
     */
    @Suppress("SwallowedException")
    fun selectResourceFromChapter(chapter: Chapter): Resource? {
        return try {
            chapter.listResource?.first {
                (it.type == Resource.Type.DASH || it.type == Resource.Type.HLS || it.type == Resource.Type.PROGRESSIVE) &&
                    (it.drmList == null || it.drmList.any { drm -> drm.type == Drm.Type.WIDEVINE })
            }
        } catch (e: NoSuchElementException) {
            null
        }
    }
}
