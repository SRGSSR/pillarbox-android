/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests inspired by https://github.com/androidx/media/blob/839c4a90f2ab36e48be73e1b5e907f3283dce72e/libraries/common/src/test/java/androidx/media3/common/ForwardingSimpleBasePlayerTest.java#L60-L78
 */
@RunWith(AndroidJUnit4::class)
class PillarboxExoPlayerTest {

    @Test
    fun `check all default method are implemented`() {
        val defaultMethod = ExoPlayer::class.java.declaredMethods.filter { it.isDefault }
        for (method in defaultMethod) {
            val name = method.name
            val parameters = method.parameterTypes
            assertEquals(PillarboxExoPlayer::class.java, PillarboxExoPlayer::class.java.getDeclaredMethod(name, *parameters).declaringClass)
        }
    }
}
