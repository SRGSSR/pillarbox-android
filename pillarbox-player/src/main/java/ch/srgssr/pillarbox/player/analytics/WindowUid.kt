/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.Timeline

/**
 * Wraps the [Timeline.Window.uid]
 */
@JvmInline
value class WindowUid(private val value: Any)

/**
 * @return the wrapped [Timeline.Window.uid]
 */
fun Timeline.Window.getWindowUid(): WindowUid {
    return WindowUid(this.uid)
}
