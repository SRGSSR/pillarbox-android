/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class PendingIntentUtilsTest {
    @Test
    fun `get default pending intent`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            setPackage(context.packageName)
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = context.packageName
                name = "LauncherActivity"
            }
        }

        shadowOf(context.packageManager).addResolveInfoForIntent(launchIntent, resolveInfo)

        val defaultPendingIntent = PendingIntentUtils.getDefaultPendingIntent(context)

        assertNotNull(defaultPendingIntent)
    }

    @Test
    fun `get default pending intent, no launch intent`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val defaultPendingIntent = PendingIntentUtils.getDefaultPendingIntent(context)

        assertNull(defaultPendingIntent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `append immutable flag if needed, before API 23`() {
        val flags = PendingIntent.FLAG_ONE_SHOT
        val immutableFlags = PendingIntentUtils.appendImmutableFlagIfNeeded(flags)

        assertEquals(0, immutableFlags and PendingIntent.FLAG_IMMUTABLE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M, Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun `append immutable flag if needed, from API 23`() {
        val flags = PendingIntent.FLAG_ONE_SHOT
        val immutableFlags = PendingIntentUtils.appendImmutableFlagIfNeeded(flags)

        assertEquals(PendingIntent.FLAG_IMMUTABLE, immutableFlags and PendingIntent.FLAG_IMMUTABLE)
    }
}
