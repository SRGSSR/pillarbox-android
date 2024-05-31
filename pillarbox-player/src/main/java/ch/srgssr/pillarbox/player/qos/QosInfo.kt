/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.VideoSize
import kotlin.time.Duration

/**
 * Represents quality of service (QoS) information for a media stream.
 *
 * @property loadTime The time it took to load the media.
 * @property playTime The duration of the media that has been played.
 * @property videoSize The size of the video, if applicable.
 * @property droppedFrames The number of frames that have been dropped.
 * @property errors The number of errors that have occurred.
 */
data class QosInfo(
    val loadTime: LoadTime = LoadTime.Empty,
    val playTime: Duration = Duration.ZERO,
    val videoSize: VideoSize = VideoSize.UNKNOWN,
    val droppedFrames: Int = 0,
    val errors: Int = 0,
) {
    companion object {
        /**
         * An empty instance of `QosInfo`.
         */
        val Empty = QosInfo()
    }
}
