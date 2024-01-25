/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.commandersact.TCEventExtensions.addAdditionalParameterIfNotBlank
import com.tagcommander.lib.serverside.events.TCCustomEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class TCEventExtensionsTest {
    @Test
    fun `add additional parameter`() {
        val tcEvent = TCCustomEvent("name")
        tcEvent.addAdditionalParameterIfNotBlank("key", "value")

        val expectedProperties = mapOf<String, Any>(
            "key" to "value"
        )

        assertEquals(expectedProperties, tcEvent.additionalProperties)
    }

    @Test
    fun `add additional parameter with null data`() {
        val tcEvent = TCCustomEvent("name")
        tcEvent.addAdditionalParameterIfNotBlank("key", null)

        assertEquals(emptyMap<String, Any>(), tcEvent.additionalProperties)
    }

    @Test
    fun `add additional parameter with empty data`() {
        val tcEvent = TCCustomEvent("name")
        tcEvent.addAdditionalParameterIfNotBlank("key", "")

        assertEquals(emptyMap<String, Any>(), tcEvent.additionalProperties)
    }

    @Test
    fun `add additional parameter with blank data`() {
        val tcEvent = TCCustomEvent("name")
        tcEvent.addAdditionalParameterIfNotBlank("key", " ")

        assertEquals(emptyMap<String, Any>(), tcEvent.additionalProperties)
    }
}
