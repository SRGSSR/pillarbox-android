/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.Test
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
}
