/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import android.os.Parcelable
import com.tagcommander.lib.serverside.events.base.TCEvent
import kotlinx.parcelize.Parcelize
import kotlin.collections.iterator

/**
 * @property pageId id of the source page → page from which the media was played e.g. homepage, detail page, search, ...
 * @property pageVersion version of the source page (e.g. for A/B test).
 * @property sectionId id of the source section → section from which the media was played.
 * @property sectionVersion version of the source section (e.g. for A/B test).
 * @property sectionPosition position of the section in the page.
 * @property itemPositionInSection position of the item in the source section.
 * @property labels A map of custom labels to be associated with the page view event. Blank values are ignored and not sent. Defaults to an empty map.
 */
@Parcelize
data class CommandersActSource(
    val pageId: String,
    val pageVersion: String? = null,
    val sectionId: String,
    val sectionVersion: String? = null,
    val sectionPosition: Int? = null,
    val itemPositionInSection: Int? = null,
    val labels: Map<String, String>? = null,
) : Parcelable {

    internal fun TCEvent.setCommandersActSource() {
        labels?.let {
            for (customEntry in it) {
                addAdditionalParameterIfNotBlank(customEntry.key, customEntry.value)
            }
        }
        addAdditionalParameterIfNotBlank(CommandersActLabels.PAGE_ID.label, pageId)
        addAdditionalParameterIfNotBlank(CommandersActLabels.PAGE_VERSION.label, pageVersion)
        addAdditionalParameterIfNotBlank(CommandersActLabels.SECTION_ID.label, sectionId)
        addAdditionalParameterIfNotBlank(CommandersActLabels.SECTION_VERSION.label, sectionVersion)
        sectionPosition?.let {
            addAdditionalProperty(CommandersActLabels.SECTION_POSITION_IN_PAGE.label, it)
        }
        itemPositionInSection?.let {
            addAdditionalProperty(CommandersActLabels.ITEM_POSITION_IN_SECTION.label, it)
        }
    }
}
