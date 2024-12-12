/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data

import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList

/**
 * Represents a section in the "Lists" tab.
 *
 * @property title The title of the section.
 * @property contentList The list of elements in the section.
 * @property languageTag The IETF BCP47 language tag of the title.
 */
data class ContentListSection(
    val title: String,
    val contentList: List<ContentList>,
    val languageTag: String?,
)
