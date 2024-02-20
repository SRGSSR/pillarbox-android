/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector.getVector
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowUIModeManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class VectorTest {
    private lateinit var context: Context
    private lateinit var uiModeManager: ShadowUIModeManager

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        uiModeManager = ShadowUIModeManager()
    }

    @Test
    fun getVector() {
        assertEquals(Vector.MOBILE, context.getVector())
    }

    @Test
    @Config(qualifiers = "appliance")
    fun `getVector appliance`() {
        assertEquals(Vector.MOBILE, context.getVector())
    }

    @Test
    @Config(qualifiers = "car")
    fun `getVector car`() {
        assertEquals(Vector.MOBILE, context.getVector())
    }

    @Test
    @Config(qualifiers = "desk")
    fun `getVector desk`() {
        assertEquals(Vector.MOBILE, context.getVector())
    }

    @Test
    @Config(qualifiers = "television")
    fun `getVector television`() {
        assertEquals(Vector.TV, context.getVector())
    }

    @Test
    @Config(qualifiers = "vrheadset")
    fun `getVector vrheadset`() {
        assertEquals(Vector.MOBILE, context.getVector())
    }

    @Test
    @Config(qualifiers = "watch")
    fun `getVector watch`() {
        assertEquals(Vector.MOBILE, context.getVector())
    }
}
